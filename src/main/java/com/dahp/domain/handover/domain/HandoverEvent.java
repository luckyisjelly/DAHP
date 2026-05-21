package com.dahp.domain.handover.domain;

import com.dahp.domain.handover.exception.AccessTokenCancelledException;
import com.dahp.domain.handover.exception.AccessTokenExpiredException;
import com.dahp.domain.handover.exception.AccessTokenUsedException;
import com.dahp.domain.handover.exception.HandoverEventAccessDeniedException;
import com.dahp.domain.handover.exception.InvalidStateTransitionException;
import com.dahp.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "handover_events",
        indexes = {
                @Index(name = "idx_event_token_hash", columnList = "access_token_hash", unique = true),
                @Index(name = "idx_event_owner_status", columnList = "owner_id, status"),
                @Index(name = "idx_event_rule_status", columnList = "rule_id, status"),
                @Index(name = "idx_event_recipient_status", columnList = "recipient_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HandoverEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HandoverEventStatus status;

    @Column(name = "access_token_hash", nullable = false, length = 64)
    private String accessTokenHash;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accessed_at")
    private LocalDateTime accessedAt;

    public static HandoverEvent issue(Long ruleId,
                                      Long ownerId,
                                      Long assetId,
                                      Long recipientId,
                                      String accessTokenHash,
                                      LocalDateTime expiresAt) {
        HandoverEvent event = new HandoverEvent();
        event.ruleId = ruleId;
        event.ownerId = ownerId;
        event.assetId = assetId;
        event.recipientId = recipientId;
        event.accessTokenHash = accessTokenHash;
        event.triggeredAt = LocalDateTime.now();
        event.expiresAt = expiresAt;
        event.status = HandoverEventStatus.PENDING;
        return event;
    }

    public void markNotified() {
        if (this.status == HandoverEventStatus.PENDING) {
            this.status = HandoverEventStatus.NOTIFIED;
        }
    }

    public void markAccessed() {
        validateAccessible();
        this.accessedAt = LocalDateTime.now();
        this.status = HandoverEventStatus.ACCESSED;
    }

    public void cancel() {
        if (this.status != HandoverEventStatus.PENDING && this.status != HandoverEventStatus.NOTIFIED) {
            throw new InvalidStateTransitionException(this.status, "CANCELLED");
        }
        this.status = HandoverEventStatus.CANCELLED;
    }

    public void markExpired() {
        if (this.status == HandoverEventStatus.PENDING || this.status == HandoverEventStatus.NOTIFIED) {
            this.status = HandoverEventStatus.EXPIRED;
        }
    }

    public void assertOwnedBy(Long userId) {
        if (!this.ownerId.equals(userId)) {
            throw new HandoverEventAccessDeniedException();
        }
    }

    /**
     * 수령인 접근 가능 여부 검증. 실패 시 적절한 예외 throw.
     */
    public void validateAccessible() {
        if (this.status == HandoverEventStatus.ACCESSED || this.accessedAt != null) {
            throw new AccessTokenUsedException();
        }
        if (this.status == HandoverEventStatus.CANCELLED) {
            throw new AccessTokenCancelledException();
        }
        if (this.status == HandoverEventStatus.EXPIRED || LocalDateTime.now().isAfter(this.expiresAt)) {
            throw new AccessTokenExpiredException();
        }
    }
}
