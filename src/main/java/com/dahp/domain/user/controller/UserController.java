package com.dahp.domain.user.controller;

import com.dahp.domain.user.application.UserService;
import com.dahp.domain.user.controller.dto.UpdateUserRequest;
import com.dahp.domain.user.controller.dto.UserResponse;
import com.dahp.global.response.ApiResponse;
import com.dahp.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "User", description = "내 사용자 정보 관리")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회")
    public ApiResponse<UserResponse> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.ok(userService.getMe(userDetails.getUserId()));
    }

    @PatchMapping("/me")
    @Operation(summary = "내 정보 수정", description = "체크인 주기 등 부분 수정. null 필드는 무시.")
    public ApiResponse<UserResponse> updateMe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse updated = userService.updateMe(userDetails.getUserId(), request);
        return ApiResponse.ok("내 정보가 수정되었습니다.", updated);
    }
}
