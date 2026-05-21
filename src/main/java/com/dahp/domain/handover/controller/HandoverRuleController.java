package com.dahp.domain.handover.controller;

import com.dahp.domain.handover.application.HandoverRuleService;
import com.dahp.domain.handover.controller.dto.HandoverRuleCreateRequest;
import com.dahp.domain.handover.controller.dto.HandoverRuleResponse;
import com.dahp.domain.handover.controller.dto.HandoverRuleUpdateRequest;
import com.dahp.domain.handover.controller.dto.HandoverTriggerResponse;
import com.dahp.domain.handover.domain.HandoverRuleStatus;
import com.dahp.global.response.ApiResponse;
import com.dahp.global.response.PageResponse;
import com.dahp.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/handover-rules")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "HandoverRule", description = "디지털 자산 인계 규칙 정의 및 관리")
public class HandoverRuleController {

    private final HandoverRuleService ruleService;

    @PostMapping
    @Operation(summary = "인계 규칙 생성", description = "자산 N개 + 수령인 N명을 묶어 조건부 인계 규칙을 정의합니다.")
    public ResponseEntity<ApiResponse<HandoverRuleResponse>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody HandoverRuleCreateRequest request) {
        HandoverRuleResponse created = ruleService.create(userDetails.getUserId(), request);
        String message = String.format("인계 규칙 '%s'이(가) DRAFT 상태로 생성되었습니다. activate API로 활성화하세요.",
                created.title());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(message, created));
    }

    @GetMapping
    @Operation(summary = "인계 규칙 목록 (상태 필터, 페이징)")
    public ApiResponse<PageResponse<HandoverRuleResponse>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) HandoverRuleStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<HandoverRuleResponse> page = ruleService.list(userDetails.getUserId(), status, pageable);
        return ApiResponse.ok(String.format("인계 규칙 총 %d건.", page.totalElements()), page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "인계 규칙 상세 (포함된 자산/수령인 같이)")
    public ApiResponse<HandoverRuleResponse> get(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.ok(ruleService.get(userDetails.getUserId(), id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "인계 규칙 수정",
            description = "title/description/conditionType/conditionValue 부분 수정. assetIds/recipientIds 전달 시 전체 교체.")
    public ApiResponse<HandoverRuleResponse> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody HandoverRuleUpdateRequest request) {
        HandoverRuleResponse updated = ruleService.update(userDetails.getUserId(), id, request);
        return ApiResponse.ok(String.format("인계 규칙 #%d이(가) 수정되었습니다.", id), updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "인계 규칙 삭제")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        ruleService.delete(userDetails.getUserId(), id);
        return ApiResponse.ok(String.format("인계 규칙 #%d이(가) 삭제되었습니다.", id));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "인계 규칙 활성화", description = "DRAFT/PAUSED → ACTIVE 상태 전이")
    public ApiResponse<HandoverRuleResponse> activate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        HandoverRuleResponse activated = ruleService.activate(userDetails.getUserId(), id);
        return ApiResponse.ok(String.format("인계 규칙 #%d이(가) 활성화되었습니다.", id), activated);
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "인계 규칙 일시정지", description = "ACTIVE → PAUSED 상태 전이")
    public ApiResponse<HandoverRuleResponse> pause(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        HandoverRuleResponse paused = ruleService.pause(userDetails.getUserId(), id);
        return ApiResponse.ok(String.format("인계 규칙 #%d이(가) 일시정지되었습니다.", id), paused);
    }

    @PostMapping("/{id}/trigger")
    @Operation(summary = "인계 규칙 수동 트리거",
            description = "ACTIVE 상태 규칙을 즉시 트리거. 자산×수령인 cross product로 이벤트가 생성되고 콘솔에 알림이 출력됩니다. 토큰 원본은 알림에만 노출됩니다.")
    public ApiResponse<HandoverTriggerResponse> trigger(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        HandoverTriggerResponse triggered = ruleService.trigger(userDetails.getUserId(), id);
        String message = String.format(
                "인계 규칙 #%d 트리거됨. %d개의 인계 이벤트가 생성되어 콘솔(서버 로그)에 알림이 출력되었습니다. " +
                        "토큰은 로그에서 확인 후 GET /api/handover-access/{token} 으로 접근하세요.",
                id, triggered.eventCount());
        return ApiResponse.ok(message, triggered);
    }
}
