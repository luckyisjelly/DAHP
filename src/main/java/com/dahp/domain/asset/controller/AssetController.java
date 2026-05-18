package com.dahp.domain.asset.controller;

import com.dahp.domain.asset.application.AssetService;
import com.dahp.domain.asset.controller.dto.AssetCreateRequest;
import com.dahp.domain.asset.controller.dto.AssetResponse;
import com.dahp.domain.asset.controller.dto.AssetUpdateRequest;
import com.dahp.domain.asset.domain.AssetType;
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
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Asset", description = "디지털 자산 CRUD")
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    @Operation(summary = "자산 생성")
    public ResponseEntity<ApiResponse<AssetResponse>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AssetCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(assetService.create(userDetails.getUserId(), request)));
    }

    @GetMapping
    @Operation(summary = "자산 목록 (타입/검색어 필터, 페이징)")
    public ApiResponse<PageResponse<AssetResponse>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) AssetType type,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(assetService.list(userDetails.getUserId(), type, q, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "자산 상세")
    public ApiResponse<AssetResponse> get(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.ok(assetService.get(userDetails.getUserId(), id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "자산 부분 수정 (null 필드는 무시)")
    public ApiResponse<AssetResponse> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AssetUpdateRequest request) {
        return ApiResponse.ok(assetService.update(userDetails.getUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "자산 삭제")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        assetService.delete(userDetails.getUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
