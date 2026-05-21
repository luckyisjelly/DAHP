package com.dahp.domain.handover.controller.dto;

import com.dahp.domain.handover.domain.HandoverRuleStatus;

import java.util.List;

public record HandoverTriggerResponse(
        Long ruleId,
        HandoverRuleStatus ruleStatus,
        int eventCount,
        List<HandoverEventResponse> events
) {
}
