package com.dahp.domain.handover.controller;

import com.dahp.domain.handover.application.HandoverEventService;
import com.dahp.domain.handover.controller.dto.HandoverEventResponse;
import com.dahp.domain.handover.domain.HandoverEventStatus;
import com.dahp.global.response.ApiResponse;
import com.dahp.global.response.PageResponse;
import com.dahp.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/handover-events")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "HandoverEvent", description = "내가 발생시킨 인계 이벤트 조회/취소 (소유자 시점)")
public class HandoverEventController {

    private final HandoverEventService eventService;

    @GetMapping
    @Operation(summary = "내 인계 이벤트 목록 (상태 필터, 페이징)")
    public ApiResponse<PageResponse<HandoverEventResponse>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) HandoverEventStatus status,
            @PageableDefault(size = 20, sort = "triggeredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<HandoverEventResponse> page = eventService.listOwn(userDetails.getUserId(), status, pageable);
        return ApiResponse.ok(String.format("인계 이벤트 총 %d건.", page.totalElements()), page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "인계 이벤트 상세")
    public ApiResponse<HandoverEventResponse> get(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.ok(eventService.get(userDetails.getUserId(), id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "인계 이벤트 취소",
            description = "PENDING/NOTIFIED 상태의 이벤트만 취소 가능. 취소된 토큰은 더 이상 접근할 수 없습니다.")
    public ApiResponse<HandoverEventResponse> cancel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        HandoverEventResponse cancelled = eventService.cancel(userDetails.getUserId(), id);
        return ApiResponse.ok(String.format("인계 이벤트 #%d이(가) 취소되었습니다.", id), cancelled);
    }
}
