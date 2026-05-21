package com.dahp.domain.handover.domain;

public enum HandoverRuleStatus {
    /** 생성 직후 — 평가 대상 아님 */
    DRAFT,
    /** 활성화됨 — 평가/트리거 대상 */
    ACTIVE,
    /** 일시 정지 */
    PAUSED,
    /** 트리거 발생 — 이벤트 생성됨 */
    TRIGGERED,
    /** 모든 이벤트 종료 */
    COMPLETED,
    /** 명시적 취소 */
    CANCELLED
}
