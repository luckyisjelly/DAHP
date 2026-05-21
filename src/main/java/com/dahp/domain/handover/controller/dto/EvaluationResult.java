package com.dahp.domain.handover.controller.dto;

import java.util.List;

public record EvaluationResult(
        int evaluatedCount,
        int triggeredCount,
        List<Long> triggeredRuleIds
) {
}
