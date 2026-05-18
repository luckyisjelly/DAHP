package com.dahp.domain.user.controller.dto;

import com.dahp.domain.user.domain.User;
import com.dahp.domain.user.domain.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        UserRole role,
        Integer checkInIntervalDays,
        LocalDateTime lastCheckInAt,
        LocalDateTime nextCheckInDueAt,
        LocalDateTime createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCheckInIntervalDays(),
                user.getLastCheckInAt(),
                user.getNextCheckInDueAt(),
                user.getCreatedAt()
        );
    }
}
