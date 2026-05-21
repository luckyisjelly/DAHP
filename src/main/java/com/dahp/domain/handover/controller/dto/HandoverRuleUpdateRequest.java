package com.dahp.domain.handover.controller.dto;

import com.dahp.domain.handover.domain.HandoverConditionType;
import jakarta.validation.constraints.Size;

import java.util.List;

public record HandoverRuleUpdateRequest(
        @Size(max = 200)
        String title,

        String description,

        HandoverConditionType conditionType,

        @Size(max = 500)
        String conditionValue,

        /** null이면 자산 목록 변경 없음. 비어있지 않은 리스트면 교체. */
        List<Long> assetIds,

        /** null이면 수령인 목록 변경 없음. 비어있지 않은 리스트면 교체. */
        List<Long> recipientIds
) {
}
