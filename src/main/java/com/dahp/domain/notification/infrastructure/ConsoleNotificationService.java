package com.dahp.domain.notification.infrastructure;

import com.dahp.domain.asset.domain.DigitalAsset;
import com.dahp.domain.handover.domain.HandoverRule;
import com.dahp.domain.notification.domain.NotificationService;
import com.dahp.domain.recipient.domain.Recipient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MVP용 콘솔 알림 구현체.
 * 실제 이메일/SMS 발송 대신 System.out + 로그에 출력.
 * P2에서 SmtpNotificationService 등으로 교체.
 */
@Slf4j
@Component
public class ConsoleNotificationService implements NotificationService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void notifyHandoverTriggered(Recipient recipient,
                                        DigitalAsset asset,
                                        HandoverRule rule,
                                        String rawToken,
                                        LocalDateTime expiresAt) {
        String banner = "=".repeat(70);
        String body = """
                %s
                [NOTIFY] 인계 알림 (인계 규칙 '%s' 발동)
                  수령인  : %s <%s>
                  자산    : '%s' (id=%d, type=%s)
                  접근 링크: GET /api/handover-access/%s
                  만료    : %s (1회만 사용 가능)
                %s
                """.formatted(
                banner,
                rule.getTitle(),
                recipient.getName(), recipient.getEmail(),
                asset.getTitle(), asset.getId(), asset.getType(),
                rawToken,
                expiresAt.format(FMT),
                banner
        );
        log.info(body);
    }
}
