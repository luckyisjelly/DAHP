package com.dahp.domain.auth.controller.dto;

import com.dahp.domain.user.domain.User;
import com.dahp.domain.user.domain.UserRole;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserInfo user
) {

    public record UserInfo(Long id, String email, UserRole role) {
        public static UserInfo from(User user) {
            return new UserInfo(user.getId(), user.getEmail(), user.getRole());
        }
    }
}
