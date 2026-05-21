package com.dahp.domain.handover.domain;

public enum HandoverEventStatus {
    /** 이벤트 생성, 알림 발송 전 */
    PENDING,
    /** 수령인에게 알림 발송 완료 */
    NOTIFIED,
    /** 수령인이 토큰으로 접근 완료 (1회 사용 끝) */
    ACCESSED,
    /** 미접근으로 만료 */
    EXPIRED,
    /** 소유자가 취소 */
    CANCELLED
}
