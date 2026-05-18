package com.dahp.domain.asset.domain;

import com.dahp.domain.asset.exception.AssetAccessDeniedException;
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

@Entity
@Table(
        name = "digital_assets",
        indexes = {
                @Index(name = "idx_asset_owner", columnList = "owner_id"),
                @Index(name = "idx_asset_owner_type", columnList = "owner_id, type")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DigitalAsset extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "content_encrypted", nullable = false)
    private boolean contentEncrypted;

    @Column(name = "external_ref", length = 500)
    private String externalRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensitivity_level", nullable = false, length = 20)
    private SensitivityLevel sensitivityLevel;

    @Builder
    private DigitalAsset(Long ownerId,
                         String title,
                         AssetType type,
                         String description,
                         String content,
                         boolean contentEncrypted,
                         String externalRef,
                         SensitivityLevel sensitivityLevel) {
        this.ownerId = ownerId;
        this.title = title;
        this.type = type;
        this.description = description;
        this.content = content;
        this.contentEncrypted = contentEncrypted;
        this.externalRef = externalRef;
        this.sensitivityLevel = sensitivityLevel != null ? sensitivityLevel : SensitivityLevel.MEDIUM;
    }

    public void update(String title,
                       AssetType type,
                       String description,
                       String content,
                       boolean contentEncrypted,
                       String externalRef,
                       SensitivityLevel sensitivityLevel) {
        if (title != null) this.title = title;
        if (type != null) this.type = type;
        if (description != null) this.description = description;
        if (content != null) {
            this.content = content;
            this.contentEncrypted = contentEncrypted;
        }
        if (externalRef != null) this.externalRef = externalRef;
        if (sensitivityLevel != null) this.sensitivityLevel = sensitivityLevel;
    }

    public void assertOwnedBy(Long userId) {
        if (!this.ownerId.equals(userId)) {
            throw new AssetAccessDeniedException();
        }
    }
}
