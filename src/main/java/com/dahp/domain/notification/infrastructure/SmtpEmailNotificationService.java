package com.dahp.domain.notification.infrastructure;

import com.dahp.domain.asset.domain.DigitalAsset;
import com.dahp.domain.handover.domain.HandoverRule;
import com.dahp.domain.notification.domain.NotificationService;
import com.dahp.domain.recipient.domain.Recipient;
import com.dahp.domain.user.domain.User;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * SMTP(Gmail 등)로 수령인에게 실제 이메일 발송.
 * 활성 조건: dahp.notification.email.enabled=true 일 때만 빈 등록됨.
 *
 * 활성 시 ConsoleNotificationService와 함께 broadcast됨 (HandoverEventService가 List 주입).
 * 메일 발송 실패는 trigger 전체를 실패시키지 않도록 catch만 하고 로그 남김.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "dahp.notification.email.enabled", havingValue = "true")
public class SmtpEmailNotificationService implements NotificationService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm");

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final String fromName;
    private final String frontendBaseUrl;
    private final String accessPath;

    public SmtpEmailNotificationService(
            JavaMailSender mailSender,
            @Value("${dahp.notification.email.from}") String fromEmail,
            @Value("${dahp.notification.email.from-name}") String fromName,
            @Value("${dahp.frontend.base-url}") String frontendBaseUrl,
            @Value("${dahp.frontend.access-path}") String accessPath) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.frontendBaseUrl = frontendBaseUrl;
        this.accessPath = accessPath;
    }

    @Override
    public void notifyHandoverTriggered(Recipient recipient,
                                        DigitalAsset asset,
                                        HandoverRule rule,
                                        User owner,
                                        String rawToken,
                                        LocalDateTime expiresAt) {
        String accessUrl = String.format("%s%s/%s",
                stripTrailingSlash(frontendBaseUrl),
                ensureLeadingSlash(accessPath),
                rawToken);

        String subject = String.format("[DAHP] %s님이 회원님께 디지털 자산을 인계했습니다",
                ownerDisplayName(owner));
        String htmlBody = buildHtml(recipient, asset, rule, owner, accessUrl, expiresAt);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(recipient.getEmail());
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("[Email] 발송 완료: to={}, asset='{}' (id={})",
                    recipient.getEmail(), asset.getTitle(), asset.getId());
        } catch (UnsupportedEncodingException | jakarta.mail.MessagingException e) {
            log.error("[Email] 메시지 구성 실패: to={}, asset={}", recipient.getEmail(), asset.getId(), e);
        } catch (Exception e) {
            log.error("[Email] SMTP 발송 실패: to={}, asset={} — {}",
                    recipient.getEmail(), asset.getId(), e.getMessage(), e);
        }
    }

    private String buildHtml(Recipient recipient,
                             DigitalAsset asset,
                             HandoverRule rule,
                             User owner,
                             String accessUrl,
                             LocalDateTime expiresAt) {
        String ownerName = ownerDisplayName(owner);
        String descriptionBlock = asset.getDescription() != null && !asset.getDescription().isBlank()
                ? String.format("<p style=\"margin:8px 0 0;color:#475569;\">%s</p>", escape(asset.getDescription()))
                : "";
        String ruleDescBlock = rule.getDescription() != null && !rule.getDescription().isBlank()
                ? String.format("<p style=\"margin:4px 0 0;color:#64748b;font-size:13px;\">%s</p>", escape(rule.getDescription()))
                : "";

        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>DAHP 인계 알림</title>
                </head>
                <body style="margin:0;padding:0;background-color:#f1f5f9;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;color:#0f172a;">
                  <div style="max-width:600px;margin:40px auto;background-color:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 12px rgba(15,23,42,0.08);">

                    <div style="background:linear-gradient(135deg,#1e3a8a,#2563eb);padding:32px 40px;color:#ffffff;">
                      <div style="font-size:13px;letter-spacing:1px;opacity:0.85;">DAHP — Digital Asset Handover Platform</div>
                      <h1 style="margin:8px 0 0;font-size:22px;font-weight:600;">디지털 자산이 인계되었습니다</h1>
                    </div>

                    <div style="padding:32px 40px;">
                      <p style="margin:0 0 16px;font-size:15px;line-height:1.6;">
                        <strong>%s</strong>님께,<br>
                        <strong>%s</strong>님이 회원님께 아래 디지털 자산을 인계하셨습니다.
                      </p>

                      <div style="background-color:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:20px;margin:24px 0;">
                        <div style="font-size:12px;color:#64748b;letter-spacing:0.5px;text-transform:uppercase;">자산</div>
                        <div style="font-size:18px;font-weight:600;margin-top:4px;">%s</div>
                        <div style="display:inline-block;margin-top:8px;padding:2px 10px;background-color:#dbeafe;color:#1e40af;border-radius:4px;font-size:12px;">%s</div>
                        %s

                        <div style="margin-top:16px;padding-top:16px;border-top:1px solid #e2e8f0;">
                          <div style="font-size:12px;color:#64748b;letter-spacing:0.5px;text-transform:uppercase;">인계 규칙</div>
                          <div style="font-size:14px;font-weight:500;margin-top:4px;">%s</div>
                          %s
                        </div>
                      </div>

                      <div style="text-align:center;margin:32px 0;">
                        <a href="%s" style="display:inline-block;padding:14px 32px;background-color:#2563eb;color:#ffffff;text-decoration:none;border-radius:8px;font-weight:600;font-size:15px;">자산 확인하기</a>
                      </div>

                      <div style="background-color:#fef3c7;border-left:4px solid #f59e0b;padding:12px 16px;border-radius:4px;margin:16px 0;">
                        <p style="margin:0;font-size:13px;color:#78350f;line-height:1.6;">
                          ⚠️ <strong>중요:</strong> 이 링크는 <strong>1회만 사용</strong> 가능합니다.<br>
                          만료 시각: <strong>%s</strong>까지 접근해야 합니다.
                        </p>
                      </div>

                      <details style="margin-top:24px;">
                        <summary style="cursor:pointer;font-size:13px;color:#64748b;">링크가 작동하지 않나요?</summary>
                        <p style="margin:8px 0 0;font-size:12px;color:#64748b;word-break:break-all;">
                          아래 URL을 브라우저 주소창에 직접 붙여넣으세요:<br>
                          <code style="background:#f1f5f9;padding:4px 6px;border-radius:4px;display:inline-block;margin-top:4px;">%s</code>
                        </p>
                      </details>
                    </div>

                    <div style="background-color:#f8fafc;padding:20px 40px;border-top:1px solid #e2e8f0;font-size:12px;color:#94a3b8;line-height:1.5;">
                      이 메일은 DAHP 자동 발송 메일입니다. 본인이 인계 대상이 아닌 경우 무시해주세요.<br>
                      문의: %s
                    </div>

                  </div>
                </body>
                </html>
                """.formatted(
                escape(recipient.getName()),
                escape(ownerName),
                escape(asset.getTitle()),
                asset.getType().name(),
                descriptionBlock,
                escape(rule.getTitle()),
                ruleDescBlock,
                accessUrl,
                expiresAt.format(FMT),
                accessUrl,
                escape(owner.getEmail())
        );
    }

    private static String ownerDisplayName(User owner) {
        String email = owner.getEmail();
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String stripTrailingSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    private static String ensureLeadingSlash(String s) {
        return s.startsWith("/") ? s : "/" + s;
    }
}
