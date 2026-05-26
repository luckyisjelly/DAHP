package com.dahp.domain.notification.infrastructure;

import com.dahp.domain.asset.domain.DigitalAsset;
import com.dahp.domain.handover.domain.HandoverRule;
import com.dahp.domain.notification.domain.NotificationService;
import com.dahp.domain.recipient.domain.Recipient;
import com.dahp.domain.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 항상 활성 (디버깅/시연 백업용). SMTP 발송과 별개로 서버 로그에도 토큰 출력.
 */
@Slf4j
@Component
public class ConsoleNotificationService implements NotificationService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void notifyHandoverTriggered(Recipient recipient,
                                        DigitalAsset asset,
                                        HandoverRule rule,
                                        User owner,
                                        String rawToken,
                                        LocalDateTime expiresAt) {
        String banner = "=".repeat(70);
        String body = """
                %s
                [NOTIFY/CONSOLE] 인계 알림 (인계 규칙 '%s' 발동)
                  소유자  : %s
                  수령인  : %s <%s>
                  자산    : '%s' (id=%d, type=%s)
                  접근 링크: GET /api/handover-access/%s
                  만료    : %s (1회만 사용 가능)
                %s
                """.formatted(
                banner,
                rule.getTitle(),
                owner.getEmail(),
                recipient.getName(), recipient.getEmail(),
                asset.getTitle(), asset.getId(), asset.getType(),
                rawToken,
                expiresAt.format(FMT),
                banner
        );
        log.info(body);
    }
}
