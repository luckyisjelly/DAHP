package com.dahp.domain.recipient.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record RecipientUpdateRequest(
        @Size(max = 100)
        String name,

        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 255)
        String email,

        @Size(max = 30)
        String phone,

        @Size(max = 50)
        String relationship,

        @Size(max = 500)
        String memo
) {
}
