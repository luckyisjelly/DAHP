package com.dahp.domain.notification.domain;

import com.dahp.domain.asset.domain.DigitalAsset;
import com.dahp.domain.handover.domain.HandoverRule;
import com.dahp.domain.recipient.domain.Recipient;

import java.time.LocalDateTime;

/**
 * 알림 발송 인터페이스.
 * MVP는 ConsoleNotificationService (System.out) 구현체 사용.
 * P2에서 SmtpNotificationService, SmsNotificationService 등으로 교체/추가.
 */
public interface NotificationService {

    /**
     * 인계 이벤트가 트리거되었음을 수령인에게 알림.
     *
     * @param recipient  수령인 정보 (이메일/이름 등)
     * @param asset      인계 대상 자산
     * @param rule       원본 인계 규칙
     * @param rawToken   원본 접근 토큰 (이 호출 외에는 어디에도 노출되지 않음 — DB는 해시만 저장)
     * @param expiresAt  토큰 만료 시각
     */
    void notifyHandoverTriggered(Recipient recipient,
                                 DigitalAsset asset,
                                 HandoverRule rule,
                                 String rawToken,
                                 LocalDateTime expiresAt);
}
