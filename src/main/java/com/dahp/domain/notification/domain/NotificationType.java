package com.dahp.domain.notification.domain;

public enum NotificationType {
    /** 수령인에게 인계 이벤트 발생 알림 (토큰 포함) */
    HANDOVER_TRIGGERED,
    /** 체크인 만료 임박 알림 (P2) */
    CHECK_IN_REMINDER,
    /** 인계 이벤트 만료 임박 (P2) */
    ACCESS_TOKEN_EXPIRING
}
