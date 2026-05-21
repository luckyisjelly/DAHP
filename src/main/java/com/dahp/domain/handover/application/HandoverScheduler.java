package com.dahp.domain.handover.application;

import com.dahp.domain.handover.controller.dto.EvaluationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 자동 조건 평가 + 만료 이벤트 정리 스케줄러.
 *
 * cron은 환경별 yml에서 설정:
 * - 기본(local): 1분마다 평가, 5분마다 만료 정리
 * - prod: 1시간 / 30분 단위 권장
 * - demo: 10초 / 10초 (시연용 단축)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HandoverScheduler {

    private final HandoverConditionEvaluator evaluator;
    private final HandoverEventService eventService;

    @Scheduled(cron = "${dahp.handover.evaluation-cron:0 */1 * * * *}")
    public void evaluateRules() {
        EvaluationResult result = evaluator.evaluateAll();
        if (result.evaluatedCount() > 0 || result.triggeredCount() > 0) {
            log.info("[Scheduler] 조건 평가: 검사 {}건, 트리거 {}건 (rules={})",
                    result.evaluatedCount(), result.triggeredCount(), result.triggeredRuleIds());
        }
    }

    @Scheduled(cron = "${dahp.handover.expiry-cron:0 */5 * * * *}")
    public void expireEvents() {
        int expired = eventService.expireOverdue();
        if (expired > 0) {
            log.info("[Scheduler] 만료 이벤트 EXPIRED 전이: {}건", expired);
        }
    }
}
