package com.dahp.domain.handover.controller.dto;

import com.dahp.domain.handover.domain.HandoverEvent;
import com.dahp.domain.handover.domain.HandoverEventStatus;

import java.time.LocalDateTime;

public record HandoverEventResponse(
        Long id,
        Long ruleId,
        Long assetId,
        Long recipientId,
        HandoverEventStatus status,
        LocalDateTime triggeredAt,
        LocalDateTime expiresAt,
        LocalDateTime accessedAt
) {

    public static HandoverEventResponse from(HandoverEvent event) {
        return new HandoverEventResponse(
                event.getId(),
                event.getRuleId(),
                event.getAssetId(),
                event.getRecipientId(),
                event.getStatus(),
                event.getTriggeredAt(),
                event.getExpiresAt(),
                event.getAccessedAt()
        );
    }
}
