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
        name = "handover_rule_assets",
        uniqueConstraints = @UniqueConstraint(name = "uk_rule_asset", columnNames = {"rule_id", "asset_id"}),
        indexes = {
                @Index(name = "idx_hra_rule", columnList = "rule_id"),
                @Index(name = "idx_hra_asset", columnList = "asset_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HandoverRuleAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    public HandoverRuleAsset(Long ruleId, Long assetId) {
        this.ruleId = ruleId;
        this.assetId = assetId;
    }
}
