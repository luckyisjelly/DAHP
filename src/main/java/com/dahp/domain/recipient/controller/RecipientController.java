package com.dahp.domain.recipient.controller;

import com.dahp.domain.recipient.application.RecipientService;
import com.dahp.domain.recipient.controller.dto.RecipientCreateRequest;
import com.dahp.domain.recipient.controller.dto.RecipientResponse;
import com.dahp.domain.recipient.controller.dto.RecipientUpdateRequest;
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
@RequestMapping("/api/recipients")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Recipient", description = "수령인(인계 대상) 관리")
public class RecipientController {

    private final RecipientService recipientService;

    @PostMapping
    @Operation(summary = "수령인 등록")
    public ResponseEntity<ApiResponse<RecipientResponse>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RecipientCreateRequest request) {
        RecipientResponse created = recipientService.create(userDetails.getUserId(), request);
        String message = String.format("수령인 '%s'이(가) 등록되었습니다. (id=%d)", created.name(), created.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(message, created));
    }

    @GetMapping
    @Operation(summary = "수령인 목록 (이름 검색, 페이징)")
    public ApiResponse<PageResponse<RecipientResponse>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<RecipientResponse> page = recipientService.list(userDetails.getUserId(), q, pageable);
        String message = String.format("수령인 총 %d명.", page.totalElements());
        return ApiResponse.ok(message, page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "수령인 상세")
    public ApiResponse<RecipientResponse> get(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.ok(recipientService.get(userDetails.getUserId(), id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "수령인 부분 수정 (null 필드 무시)")
    public ApiResponse<RecipientResponse> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody RecipientUpdateRequest request) {
        RecipientResponse updated = recipientService.update(userDetails.getUserId(), id, request);
        return ApiResponse.ok(String.format("수령인 #%d이(가) 수정되었습니다.", id), updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "수령인 삭제")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        recipientService.delete(userDetails.getUserId(), id);
        return ApiResponse.ok(String.format("수령인 #%d이(가) 삭제되었습니다.", id));
    }
}
