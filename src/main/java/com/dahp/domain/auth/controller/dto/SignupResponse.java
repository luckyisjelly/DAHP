package com.dahp.domain.auth.controller.dto;

import com.dahp.domain.user.domain.User;
import com.dahp.domain.user.domain.UserRole;

import java.time.LocalDateTime;

public record SignupResponse(
        Long userId,
        String email,
        UserRole role,
        Integer checkInIntervalDays,
        LocalDateTime nextCheckInDueAt,
        String nextStep
) {

    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCheckInIntervalDays(),
                user.getNextCheckInDueAt(),
                "POST /api/auth/login 으로 로그인해 access 토큰을 발급받으세요."
        );
    }
}
