package com.dahp.domain.auth.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 100, message = "비밀번호는 8~100자여야 합니다.")
        String password,

        @Min(value = 1, message = "체크인 주기는 최소 1일입니다.")
        @Max(value = 365, message = "체크인 주기는 최대 365일입니다.")
        Integer checkInIntervalDays
) {
}
