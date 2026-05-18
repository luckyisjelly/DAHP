package com.dahp.domain.auth.controller;

import com.dahp.domain.auth.application.AuthService;
import com.dahp.domain.auth.controller.dto.LoginRequest;
import com.dahp.domain.auth.controller.dto.LoginResponse;
import com.dahp.domain.auth.controller.dto.SignupRequest;
import com.dahp.domain.auth.controller.dto.TokenRefreshRequest;
import com.dahp.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "회원가입 / 로그인 / 토큰 갱신")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "회원 가입", description = "이메일/비밀번호 + 체크인 주기로 가입합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> signup(@Valid @RequestBody SignupRequest request) {
        Long userId = authService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(Map.of("userId", userId, "email", request.email())));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "성공 시 access/refresh 토큰을 발급합니다.")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "refresh 토큰으로 새 access 토큰을 발급합니다.")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }
}
