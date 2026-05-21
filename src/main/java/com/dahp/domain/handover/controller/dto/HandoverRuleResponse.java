package com.dahp.domain.handover.controller.dto;

import com.dahp.domain.asset.domain.AssetType;
import com.dahp.domain.asset.domain.DigitalAsset;
import com.dahp.domain.handover.domain.HandoverConditionType;
import com.dahp.domain.handover.domain.HandoverRule;
import com.dahp.domain.handover.domain.HandoverRuleStatus;
import com.dahp.domain.recipient.domain.Recipient;

import java.time.LocalDateTime;
import java.util.List;

public record HandoverRuleResponse(
        Long id,
        String title,
        String description,
        HandoverConditionType conditionType,
        String conditionValue,
        HandoverRuleStatus status,
        List<AssetSummary> assets,
        List<RecipientSummary> recipients,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public record AssetSummary(Long id, String title, AssetType type) {
        public static AssetSummary from(DigitalAsset a) {
            return new AssetSummary(a.getId(), a.getTitle(), a.getType());
        }
    }

    public record RecipientSummary(Long id, String name, String email) {
        public static RecipientSummary from(Recipient r) {
            return new RecipientSummary(r.getId(), r.getName(), r.getEmail());
        }
    }

    public static HandoverRuleResponse of(HandoverRule rule,
                                          List<DigitalAsset> assets,
                                          List<Recipient> recipients) {
        return new HandoverRuleResponse(
                rule.getId(),
                rule.getTitle(),
                rule.getDescription(),
                rule.getConditionType(),
                rule.getConditionValue(),
                rule.getStatus(),
                assets.stream().map(AssetSummary::from).toList(),
                recipients.stream().map(RecipientSummary::from).toList(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}
