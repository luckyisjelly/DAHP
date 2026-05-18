package com.dahp.domain.user.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateUserRequest(
        @Min(value = 1, message = "체크인 주기는 최소 1일입니다.")
        @Max(value = 365, message = "체크인 주기는 최대 365일입니다.")
        Integer checkInIntervalDays
) {
}
