package com.dahp.domain.handover.application;

import com.dahp.domain.asset.domain.AssetRepository;
import com.dahp.domain.asset.domain.DigitalAsset;
import com.dahp.domain.asset.domain.EncryptionService;
import com.dahp.domain.asset.exception.AssetNotFoundException;
import com.dahp.domain.handover.controller.dto.HandoverAccessResponse;
import com.dahp.domain.handover.controller.dto.HandoverEventResponse;
import com.dahp.domain.handover.controller.dto.HandoverTriggerResponse;
import com.dahp.domain.handover.domain.HandoverEvent;
import com.dahp.domain.handover.domain.HandoverEventRepository;
import com.dahp.domain.handover.domain.HandoverEventStatus;
import com.dahp.domain.handover.domain.HandoverRule;
import com.dahp.domain.handover.domain.HandoverRuleAsset;
import com.dahp.domain.handover.domain.HandoverRuleAssetRepository;
import com.dahp.domain.handover.domain.HandoverRuleRecipient;
import com.dahp.domain.handover.domain.HandoverRuleRecipientRepository;
import com.dahp.domain.handover.domain.HandoverRuleRepository;
import com.dahp.domain.handover.exception.AccessTokenInvalidException;
import com.dahp.domain.handover.exception.HandoverEventNotFoundException;
import com.dahp.domain.handover.exception.HandoverRuleNotFoundException;
import com.dahp.domain.notification.domain.NotificationService;
import com.dahp.domain.recipient.domain.Recipient;
import com.dahp.domain.recipient.domain.RecipientRepository;
import com.dahp.domain.user.domain.User;
import com.dahp.domain.user.domain.UserRepository;
import com.dahp.domain.user.exception.UserNotFoundException;
import com.dahp.global.response.PageResponse;
import com.dahp.global.util.TokenGenerator;
import com.dahp.global.util.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HandoverEventService {

    private final HandoverEventRepository eventRepository;
    private final HandoverRuleRepository ruleRepository;
    private final HandoverRuleAssetRepository ruleAssetRepository;
    private final HandoverRuleRecipientRepository ruleRecipientRepository;
    private final AssetRepository assetRepository;
    private final RecipientRepository recipientRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EncryptionService encryptionService;

    @Value("${dahp.handover.access-token-validity:PT72H}")
    private Duration accessTokenValidity;

    /**
     * 규칙을 트리거 — 자산×수령인 cross product로 이벤트 생성 + 알림 발송.
     * 규칙 상태 전이(ACTIVE→TRIGGERED)는 호출자(HandoverRuleService)가 책임짐.
     */
    public HandoverTriggerResponse createEventsFor(HandoverRule rule) {
        List<Long> assetIds = ruleAssetRepository.findByRuleId(rule.getId())
                .stream().map(HandoverRuleAsset::getAssetId).toList();
        List<Long> recipientIds = ruleRecipientRepository.findByRuleId(rule.getId())
                .stream().map(HandoverRuleRecipient::getRecipientId).toList();

        List<DigitalAsset> assets = assetRepository.findAllByIdIn(assetIds);
        List<Recipient> recipients = recipientRepository.findAllByIdIn(recipientIds);

        LocalDateTime expiresAt = LocalDateTime.now().plus(accessTokenValidity);
        List<HandoverEventResponse> created = new ArrayList<>();

        for (DigitalAsset asset : assets) {
            for (Recipient recipient : recipients) {
                String rawToken = TokenGenerator.generate();
                String tokenHash = TokenHasher.sha256Hex(rawToken);

                HandoverEvent event = HandoverEvent.issue(
                        rule.getId(), rule.getOwnerId(),
                        asset.getId(), recipient.getId(),
                        tokenHash, expiresAt
                );
                eventRepository.save(event);

                notificationService.notifyHandoverTriggered(recipient, asset, rule, rawToken, expiresAt);
                event.markNotified();

                created.add(HandoverEventResponse.from(event));
            }
        }

        return new HandoverTriggerResponse(rule.getId(), rule.getStatus(), created.size(), created);
    }

    @Transactional(readOnly = true)
    public PageResponse<HandoverEventResponse> listOwn(Long ownerId, HandoverEventStatus status, Pageable pageable) {
        Page<HandoverEvent> page = status != null
                ? eventRepository.findByOwnerIdAndStatus(ownerId, status, pageable)
                : eventRepository.findByOwnerId(ownerId, pageable);
        return PageResponse.from(page.map(HandoverEventResponse::from));
    }

    @Transactional(readOnly = true)
    public HandoverEventResponse get(Long ownerId, Long eventId) {
        return HandoverEventResponse.from(loadOwned(ownerId, eventId));
    }

    public HandoverEventResponse cancel(Long ownerId, Long eventId) {
        HandoverEvent event = loadOwned(ownerId, eventId);
        event.cancel();
        return HandoverEventResponse.from(event);
    }

    /**
     * 수령인 토큰 접근 — 인증 없음, 토큰 1회용.
     */
    public HandoverAccessResponse access(String rawToken) {
        String hash = TokenHasher.sha256Hex(rawToken);
        HandoverEvent event = eventRepository.findByAccessTokenHash(hash)
                .orElseThrow(AccessTokenInvalidException::new);

        event.markAccessed();  // 만료/사용됨/취소 등 검증 후 상태 ACCESSED 전이

        DigitalAsset asset = assetRepository.findById(event.getAssetId())
                .orElseThrow(AssetNotFoundException::new);
        HandoverRule rule = ruleRepository.findById(event.getRuleId())
                .orElseThrow(HandoverRuleNotFoundException::new);
        User owner = userRepository.findById(event.getOwnerId())
                .orElseThrow(UserNotFoundException::new);

        String plainContent = asset.getContent();
        if (asset.isContentEncrypted() && plainContent != null) {
            plainContent = encryptionService.decrypt(plainContent);
        }
        return HandoverAccessResponse.of(event, asset, plainContent, rule, owner);
    }

    /**
     * 스케줄러용 — 만료된 활성 이벤트(PENDING/NOTIFIED)를 EXPIRED로 일괄 전이.
     * @return 전이된 이벤트 수
     */
    public int expireOverdue() {
        List<HandoverEvent> candidates = eventRepository.findAllByStatusInAndExpiresAtBefore(
                EnumSet.of(HandoverEventStatus.PENDING, HandoverEventStatus.NOTIFIED),
                LocalDateTime.now()
        );
        candidates.forEach(HandoverEvent::markExpired);
        return candidates.size();
    }

    private HandoverEvent loadOwned(Long ownerId, Long eventId) {
        HandoverEvent event = eventRepository.findById(eventId)
                .orElseThrow(HandoverEventNotFoundException::new);
        event.assertOwnedBy(ownerId);
        return event;
    }
}
