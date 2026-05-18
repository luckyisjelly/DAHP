package com.dahp.domain.asset.controller.dto;

import com.dahp.domain.asset.domain.AssetType;
import com.dahp.domain.asset.domain.DigitalAsset;
import com.dahp.domain.asset.domain.SensitivityLevel;

import java.time.LocalDateTime;

public record AssetResponse(
        Long id,
        String title,
        AssetType type,
        String description,
        String content,
        String externalRef,
        SensitivityLevel sensitivityLevel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AssetResponse from(DigitalAsset asset, String decryptedContent) {
        return new AssetResponse(
                asset.getId(),
                asset.getTitle(),
                asset.getType(),
                asset.getDescription(),
                decryptedContent,
                asset.getExternalRef(),
                asset.getSensitivityLevel(),
                asset.getCreatedAt(),
                asset.getUpdatedAt()
        );
    }
}
