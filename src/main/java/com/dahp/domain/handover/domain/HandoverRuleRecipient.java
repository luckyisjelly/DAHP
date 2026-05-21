package com.dahp.domain.handover.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "handover_rule_recipients",
        uniqueConstraints = @UniqueConstraint(name = "uk_rule_recipient", columnNames = {"rule_id", "recipient_id"}),
        indexes = {
                @Index(name = "idx_hrr_rule", columnList = "rule_id"),
                @Index(name = "idx_hrr_recipient", columnList = "recipient_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HandoverRuleRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    public HandoverRuleRecipient(Long ruleId, Long recipientId) {
        this.ruleId = ruleId;
        this.recipientId = recipientId;
    }
}
