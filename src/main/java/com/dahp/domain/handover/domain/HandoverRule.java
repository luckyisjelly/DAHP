package com.dahp.domain.handover.domain;

import com.dahp.domain.handover.exception.HandoverRuleAccessDeniedException;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(
        name = "handover_rules",
        indexes = {
                @Index(name = "idx_rule_owner", columnList = "owner_id"),
                @Index(name = "idx_rule_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HandoverRule extends BaseEntity {

    private static final Set<HandoverRuleStatus> ACTIVATABLE_FROM =
            EnumSet.of(HandoverRuleStatus.DRAFT, HandoverRuleStatus.PAUSED);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 30)
    private HandoverConditionType conditionType;

    @Column(name = "condition_value", length = 500)
    private String conditionValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HandoverRuleStatus status;

    @Builder
    private HandoverRule(Long ownerId,
                         String title,
                         String description,
                         HandoverConditionType conditionType,
                         String conditionValue) {
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
        this.status = HandoverRuleStatus.DRAFT;
    }

    public void update(String title,
                       String description,
                       HandoverConditionType conditionType,
                       String conditionValue) {
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (conditionType != null) this.conditionType = conditionType;
        if (conditionValue != null) this.conditionValue = conditionValue;
    }

    public void activate() {
        if (!ACTIVATABLE_FROM.contains(this.status)) {
            throw new InvalidStateTransitionException(this.status, "ACTIVE");
        }
        this.status = HandoverRuleStatus.ACTIVE;
    }

    public void pause() {
        if (this.status != HandoverRuleStatus.ACTIVE) {
            throw new InvalidStateTransitionException(this.status, "PAUSED");
        }
        this.status = HandoverRuleStatus.PAUSED;
    }

    public void markTriggered() {
        if (this.status != HandoverRuleStatus.ACTIVE) {
            throw new InvalidStateTransitionException(this.status, "TRIGGERED");
        }
        this.status = HandoverRuleStatus.TRIGGERED;
    }

    public void markCompleted() {
        this.status = HandoverRuleStatus.COMPLETED;
    }

    public void cancel() {
        this.status = HandoverRuleStatus.CANCELLED;
    }

    public void assertOwnedBy(Long userId) {
        if (!this.ownerId.equals(userId)) {
            throw new HandoverRuleAccessDeniedException();
        }
    }
}
