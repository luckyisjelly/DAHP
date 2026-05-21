package com.dahp.domain.handover.application;

import com.dahp.domain.handover.controller.dto.EvaluationResult;
import com.dahp.domain.handover.domain.HandoverConditionType;
import com.dahp.domain.handover.domain.HandoverRule;
import com.dahp.domain.handover.domain.HandoverRuleRepository;
import com.dahp.domain.handover.domain.HandoverRuleStatus;
import com.dahp.domain.user.domain.User;
import com.dahp.domain.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 인계 규칙 조건 평가기.
 *
 * MANUAL_APPROVAL: 평가 대상 아님 (소유자가 직접 trigger API 호출)
 * SPECIFIC_DATE: conditionValue(ISO LocalDate, 예 "2026-12-31") <= today
 * INACTIVITY_PERIOD: conditionValue(일수)만큼 lastCheckInAt(없으면 createdAt) 경과
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class HandoverConditionEvaluator {

    private final HandoverRuleRepository ruleRepository;
    private final UserRepository userRepository;
    private final HandoverRuleService ruleService;

    public EvaluationResult evaluateAll() {
        List<HandoverRule> active = ruleRepository.findAllByStatus(HandoverRuleStatus.ACTIVE);
        return evaluate(active);
    }

    public EvaluationResult evaluateForUser(Long ownerId) {
        List<HandoverRule> active = ruleRepository.findAllByOwnerIdAndStatus(ownerId, HandoverRuleStatus.ACTIVE);
        return evaluate(active);
    }

    private EvaluationResult evaluate(List<HandoverRule> rules) {
        LocalDateTime now = LocalDateTime.now();
        Map<Long, User> ownerCache = new HashMap<>();
        List<Long> triggered = new ArrayList<>();

        for (HandoverRule rule : rules) {
            try {
                if (shouldTrigger(rule, now, ownerCache)) {
                    ruleService.trigger(rule.getOwnerId(), rule.getId());
                    triggered.add(rule.getId());
                    log.info("[Evaluator] triggered rule#{} (type={}) owner={}",
                            rule.getId(), rule.getConditionType(), rule.getOwnerId());
                }
            } catch (Exception e) {
                log.error("[Evaluator] rule#{} 평가 중 오류: {}", rule.getId(), e.getMessage(), e);
            }
        }
        return new EvaluationResult(rules.size(), triggered.size(), triggered);
    }

    private boolean shouldTrigger(HandoverRule rule, LocalDateTime now, Map<Long, User> ownerCache) {
        HandoverConditionType type = rule.getConditionType();
        String value = rule.getConditionValue();

        return switch (type) {
            case MANUAL_APPROVAL -> false;

            case SPECIFIC_DATE -> {
                LocalDate target = parseDate(value);
                yield target != null && !LocalDate.now().isBefore(target);
            }

            case INACTIVITY_PERIOD -> {
                Integer days = parseInt(value);
                if (days == null) yield false;
                User owner = ownerCache.computeIfAbsent(rule.getOwnerId(),
                        id -> userRepository.findById(id).orElse(null));
                if (owner == null) yield false;
                LocalDateTime base = Optional.ofNullable(owner.getLastCheckInAt())
                        .orElse(owner.getCreatedAt());
                yield base != null && base.plusDays(days).isBefore(now);
            }
        };
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            log.warn("conditionValue 날짜 파싱 실패: '{}'", value);
            return null;
        }
    }

    private static Integer parseInt(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("conditionValue 정수 파싱 실패: '{}'", value);
            return null;
        }
    }
}
