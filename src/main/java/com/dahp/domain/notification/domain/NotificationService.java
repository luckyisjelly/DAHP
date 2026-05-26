package com.dahp.domain.notification.domain;

import com.dahp.domain.asset.domain.DigitalAsset;
import com.dahp.domain.handover.domain.HandoverRule;
import com.dahp.domain.recipient.domain.Recipient;
import com.dahp.domain.user.domain.User;

import java.time.LocalDateTime;

/**
 * 알림 발송 인터페이스.
 * 여러 구현체가 동시에 활성화될 수 있고 (HandoverEventService에서 List로 broadcast),
 * 각 구현체는 자기 채널(콘솔/이메일/SMS 등)로만 책임짐.
 */
public interface NotificationService {

    /**
     * 인계 이벤트가 트리거되었음을 수령인에게 알림.
     *
     * @param recipient  수령인 (이메일/이름)
     * @param asset      인계 대상 자산
     * @param rule       원본 인계 규칙
     * @param owner      자산 소유자 (이메일 등 표시용)
     * @param rawToken   원본 접근 토큰 — 이 호출 외에는 어디에도 노출되지 않음 (DB는 해시만)
     * @param expiresAt  토큰 만료 시각
     */
    void notifyHandoverTriggered(Recipient recipient,
                                 DigitalAsset asset,
                                 HandoverRule rule,
                                 User owner,
                                 String rawToken,
                                 LocalDateTime expiresAt);
}
