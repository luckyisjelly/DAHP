package com.dahp.domain.checkin.controller.dto;

import com.dahp.domain.user.domain.User;

import java.time.Duration;
import java.time.LocalDateTime;

public record CheckInStatusResponse(
        LocalDateTime lastCheckInAt,
        LocalDateTime nextCheckInDueAt,
        Integer checkInIntervalDays,
        boolean overdue,
        long daysUntilDue
) {

    public static CheckInStatusResponse from(User user) {
        LocalDateTime now = LocalDateTime.now();
        boolean overdue = user.isCheckInOverdue();
        long days = user.getNextCheckInDueAt() != null
                ? Duration.between(now, user.getNextCheckInDueAt()).toDays()
                : 0L;
        return new CheckInStatusResponse(
                user.getLastCheckInAt(),
                user.getNextCheckInDueAt(),
                user.getCheckInIntervalDays(),
                overdue,
                days
        );
    }
}
