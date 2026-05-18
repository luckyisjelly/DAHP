package com.dahp.domain.auth.controller.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}
