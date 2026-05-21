package com.dahp.domain.handover.controller;

import com.dahp.domain.handover.application.HandoverConditionEvaluator;
import com.dahp.domain.handover.controller.dto.EvaluationResult;
import com.dahp.global.response.ApiResponse;
import com.dahp.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 시연/검증용 — 본인의 ACTIVE 규칙을 즉시 평가.
 * 평소엔 스케줄러가 cron으로 동작하지만, 시연 시 cron 대기 없이 강제 실행하기 위해 노출.
 */
@RestController
@RequestMapping("/api/handover/evaluation")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "HandoverEvaluation", description = "조건 평가 강제 실행 (시연/검증용)")
public class HandoverEvaluationController {

    private final HandoverConditionEvaluator evaluator;

    @PostMapping("/evaluate-mine")
    @Operation(summary = "내 ACTIVE 규칙 즉시 평가",
            description = """
                    스케줄러 cron을 기다리지 않고 본인의 모든 ACTIVE 규칙을 즉시 평가합니다.
                    조건 충족된 규칙은 자동으로 trigger되어 인계 이벤트가 생성됩니다.

                    조건별 평가:
                    - MANUAL_APPROVAL: 평가 대상 아님 (직접 trigger API 호출 필요)
                    - SPECIFIC_DATE: conditionValue(예: 2026-12-31) <= 오늘 이면 trigger
                    - INACTIVITY_PERIOD: conditionValue(일수)만큼 lastCheckInAt(없으면 createdAt) 경과 시 trigger
                    """)
    public ApiResponse<EvaluationResult> evaluateMine(@AuthenticationPrincipal CustomUserDetails userDetails) {
        EvaluationResult result = evaluator.evaluateForUser(userDetails.getUserId());
        String msg = String.format("내 ACTIVE 규칙 %d개 평가, %d개 트리거됨.",
                result.evaluatedCount(), result.triggeredCount());
        return ApiResponse.ok(msg, result);
    }
}
