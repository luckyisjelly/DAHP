package com.dahp.domain.handover.application;

import com.dahp.domain.asset.domain.AssetRepository;
import com.dahp.domain.asset.domain.DigitalAsset;
import com.dahp.domain.handover.controller.dto.HandoverRuleCreateRequest;
import com.dahp.domain.handover.controller.dto.HandoverRuleResponse;
import com.dahp.domain.handover.controller.dto.HandoverRuleUpdateRequest;
import com.dahp.domain.handover.controller.dto.HandoverTriggerResponse;
import com.dahp.domain.handover.domain.HandoverRule;
import com.dahp.domain.handover.domain.HandoverRuleAsset;
import com.dahp.domain.handover.domain.HandoverRuleAssetRepository;
import com.dahp.domain.handover.domain.HandoverRuleRecipient;
import com.dahp.domain.handover.domain.HandoverRuleRecipientRepository;
import com.dahp.domain.handover.domain.HandoverRuleRepository;
import com.dahp.domain.handover.domain.HandoverRuleStatus;
import com.dahp.domain.handover.exception.HandoverRuleAccessDeniedException;
import com.dahp.domain.handover.exception.HandoverRuleNotFoundException;
import com.dahp.domain.recipient.domain.Recipient;
import com.dahp.domain.recipient.domain.RecipientRepository;
import com.dahp.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HandoverRuleService {

    private final HandoverRuleRepository ruleRepository;
    private final HandoverRuleAssetRepository ruleAssetRepository;
    private final HandoverRuleRecipientRepository ruleRecipientRepository;
    private final AssetRepository assetRepository;
    private final RecipientRepository recipientRepository;
    private final HandoverEventService eventService;

    public HandoverRuleResponse create(Long ownerId, HandoverRuleCreateRequest request) {
        List<DigitalAsset> assets = loadOwnedAssets(ownerId, request.assetIds());
        List<Recipient> recipients = loadOwnedRecipients(ownerId, request.recipientIds());

        HandoverRule rule = HandoverRule.builder()
                .ownerId(ownerId)
                .title(request.title())
                .description(request.description())
                .conditionType(request.conditionType())
                .conditionValue(request.conditionValue())
                .build();
        ruleRepository.save(rule);

        ruleAssetRepository.saveAll(assets.stream()
                .map(a -> new HandoverRuleAsset(rule.getId(), a.getId())).toList());
        ruleRecipientRepository.saveAll(recipients.stream()
                .map(r -> new HandoverRuleRecipient(rule.getId(), r.getId())).toList());

        return HandoverRuleResponse.of(rule, assets, recipients);
    }

    @Transactional(readOnly = true)
    public PageResponse<HandoverRuleResponse> list(Long ownerId, HandoverRuleStatus status, Pageable pageable) {
        Page<HandoverRule> page = status != null
                ? ruleRepository.findByOwnerIdAndStatus(ownerId, status, pageable)
                : ruleRepository.findByOwnerId(ownerId, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public HandoverRuleResponse get(Long ownerId, Long ruleId) {
        return toResponse(loadOwned(ownerId, ruleId));
    }

    public HandoverRuleResponse update(Long ownerId, Long ruleId, HandoverRuleUpdateRequest request) {
        HandoverRule rule = loadOwned(ownerId, ruleId);
        rule.update(request.title(), request.description(), request.conditionType(), request.conditionValue());

        if (request.assetIds() != null) {
            List<DigitalAsset> assets = loadOwnedAssets(ownerId, request.assetIds());
            ruleAssetRepository.deleteByRuleId(ruleId);
            ruleAssetRepository.saveAll(assets.stream()
                    .map(a -> new HandoverRuleAsset(ruleId, a.getId())).toList());
        }
        if (request.recipientIds() != null) {
            List<Recipient> recipients = loadOwnedRecipients(ownerId, request.recipientIds());
            ruleRecipientRepository.deleteByRuleId(ruleId);
            ruleRecipientRepository.saveAll(recipients.stream()
                    .map(r -> new HandoverRuleRecipient(ruleId, r.getId())).toList());
        }

        return toResponse(rule);
    }

    public void delete(Long ownerId, Long ruleId) {
        HandoverRule rule = loadOwned(ownerId, ruleId);
        ruleAssetRepository.deleteByRuleId(ruleId);
        ruleRecipientRepository.deleteByRuleId(ruleId);
        ruleRepository.delete(rule);
    }

    public HandoverRuleResponse activate(Long ownerId, Long ruleId) {
        HandoverRule rule = loadOwned(ownerId, ruleId);
        rule.activate();
        return toResponse(rule);
    }

    public HandoverRuleResponse pause(Long ownerId, Long ruleId) {
        HandoverRule rule = loadOwned(ownerId, ruleId);
        rule.pause();
        return toResponse(rule);
    }

    /**
     * 수동 트리거 — ACTIVE 상태에서만 가능.
     * rule.status를 TRIGGERED로 전이하고 cross-product 이벤트를 생성.
     */
    public HandoverTriggerResponse trigger(Long ownerId, Long ruleId) {
        HandoverRule rule = loadOwned(ownerId, ruleId);
        rule.markTriggered();
        return eventService.createEventsFor(rule);
    }

    private HandoverRule loadOwned(Long ownerId, Long ruleId) {
        HandoverRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(HandoverRuleNotFoundException::new);
        rule.assertOwnedBy(ownerId);
        return rule;
    }

    private List<DigitalAsset> loadOwnedAssets(Long ownerId, List<Long> assetIds) {
        if (assetIds == null || assetIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<DigitalAsset> assets = assetRepository.findAllByIdInAndOwnerId(assetIds, ownerId);
        if (assets.size() != assetIds.stream().distinct().count()) {
            throw new HandoverRuleAccessDeniedException();
        }
        return assets;
    }

    private List<Recipient> loadOwnedRecipients(Long ownerId, List<Long> recipientIds) {
        if (recipientIds == null || recipientIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Recipient> recipients = recipientRepository.findAllByIdInAndOwnerId(recipientIds, ownerId);
        if (recipients.size() != recipientIds.stream().distinct().count()) {
            throw new HandoverRuleAccessDeniedException();
        }
        return recipients;
    }

    private HandoverRuleResponse toResponse(HandoverRule rule) {
        List<Long> assetIds = ruleAssetRepository.findByRuleId(rule.getId())
                .stream().map(HandoverRuleAsset::getAssetId).toList();
        List<Long> recipientIds = ruleRecipientRepository.findByRuleId(rule.getId())
                .stream().map(HandoverRuleRecipient::getRecipientId).toList();
        List<DigitalAsset> assets = assetIds.isEmpty()
                ? Collections.emptyList()
                : assetRepository.findAllByIdIn(assetIds);
        List<Recipient> recipients = recipientIds.isEmpty()
                ? Collections.emptyList()
                : recipientRepository.findAllByIdIn(recipientIds);
        return HandoverRuleResponse.of(rule, assets, recipients);
    }
}
