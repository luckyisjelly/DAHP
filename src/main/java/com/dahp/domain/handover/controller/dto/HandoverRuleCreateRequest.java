package com.dahp.domain.handover.controller.dto;

import com.dahp.domain.handover.domain.HandoverConditionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record HandoverRuleCreateRequest(
        @NotBlank(message = "규칙 제목은 필수입니다.")
        @Size(max = 200)
        String title,

        String description,

        @NotNull(message = "조건 타입은 필수입니다.")
        HandoverConditionType conditionType,

        @Size(max = 500)
        String conditionValue,

        @NotEmpty(message = "최소 1개 이상의 자산을 포함해야 합니다.")
        List<Long> assetIds,

        @NotEmpty(message = "최소 1명 이상의 수령인을 포함해야 합니다.")
        List<Long> recipientIds
) {
}
