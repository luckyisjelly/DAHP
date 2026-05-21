package com.dahp.domain.auth.controller;

import com.dahp.domain.auth.application.AuthService;
import com.dahp.domain.auth.controller.dto.LoginRequest;
import com.dahp.domain.auth.controller.dto.LoginResponse;
import com.dahp.domain.auth.controller.dto.SignupRequest;
import com.dahp.domain.auth.controller.dto.SignupResponse;
import com.dahp.domain.auth.controller.dto.TokenRefreshRequest;
import com.dahp.domain.user.domain.User;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "회원가입 / 로그인 / 토큰 갱신")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "회원 가입", description = "이메일/비밀번호 + 체크인 주기로 가입합니다.")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        User user = authService.signup(request);
        SignupResponse body = SignupResponse.from(user);
        String message = String.format("'%s'으로 회원가입이 완료되었습니다.", user.getEmail());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(message, body));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "성공 시 access/refresh 토큰과 사용자 정보를 함께 반환합니다.")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        String message = String.format("'%s'으로 로그인되었습니다. access 토큰은 %d초간 유효합니다.",
                response.user().email(), response.expiresIn());
        return ApiResponse.ok(message, response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "refresh 토큰으로 새 access 토큰을 발급합니다.")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        LoginResponse response = authService.refresh(request);
        return ApiResponse.ok("access 토큰이 갱신되었습니다.", response);
    }
}
