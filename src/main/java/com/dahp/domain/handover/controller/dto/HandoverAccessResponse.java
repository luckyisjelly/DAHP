package com.dahp.domain.handover.controller.dto;

import com.dahp.domain.asset.domain.AssetType;
import com.dahp.domain.asset.domain.DigitalAsset;
import com.dahp.domain.asset.domain.SensitivityLevel;
import com.dahp.domain.handover.domain.HandoverEvent;
import com.dahp.domain.handover.domain.HandoverRule;
import com.dahp.domain.user.domain.User;

import java.time.LocalDateTime;

public record HandoverAccessResponse(
        AssetView asset,
        RuleView rule,
        OwnerView owner,
        LocalDateTime accessedAt,
        LocalDateTime expiresAt,
        String notice
) {

    public record AssetView(
            String title,
            AssetType type,
            String description,
            String content,
            String externalRef,
            SensitivityLevel sensitivityLevel
    ) {
        public static AssetView from(DigitalAsset a, String decryptedContent) {
            return new AssetView(
                    a.getTitle(), a.getType(), a.getDescription(),
                    decryptedContent, a.getExternalRef(), a.getSensitivityLevel()
            );
        }
    }

    public record RuleView(String title, String description) {
        public static RuleView from(HandoverRule r) {
            return new RuleView(r.getTitle(), r.getDescription());
        }
    }

    public record OwnerView(String email) {
        public static OwnerView from(User u) {
            return new OwnerView(u.getEmail());
        }
    }

    public static HandoverAccessResponse of(HandoverEvent event,
                                            DigitalAsset asset,
                                            String decryptedContent,
                                            HandoverRule rule,
                                            User owner) {
        return new HandoverAccessResponse(
                AssetView.from(asset, decryptedContent),
                RuleView.from(rule),
                OwnerView.from(owner),
                event.getAccessedAt(),
                event.getExpiresAt(),
                "이 정보는 1회만 조회 가능합니다. 다시 접근할 수 없으니 필요한 내용을 지금 저장하세요."
        );
    }
}
