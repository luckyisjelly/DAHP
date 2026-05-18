package com.dahp.domain.asset.application;

import com.dahp.domain.asset.controller.dto.AssetCreateRequest;
import com.dahp.domain.asset.controller.dto.AssetResponse;
import com.dahp.domain.asset.controller.dto.AssetUpdateRequest;
import com.dahp.domain.asset.domain.AssetRepository;
import com.dahp.domain.asset.domain.AssetType;
import com.dahp.domain.asset.domain.DigitalAsset;
import com.dahp.domain.asset.domain.EncryptionService;
import com.dahp.domain.asset.exception.AssetNotFoundException;
import com.dahp.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;
    private final EncryptionService encryptionService;

    public AssetResponse create(Long ownerId, AssetCreateRequest request) {
        String storedContent = request.content() != null
                ? encryptionService.encrypt(request.content())
                : null;
        DigitalAsset asset = DigitalAsset.builder()
                .ownerId(ownerId)
                .title(request.title())
                .type(request.type())
                .description(request.description())
                .content(storedContent)
                .contentEncrypted(storedContent != null && encryptionService.isEnabled())
                .externalRef(request.externalRef())
                .sensitivityLevel(request.sensitivityLevel())
                .build();
        DigitalAsset saved = assetRepository.save(asset);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<AssetResponse> list(Long ownerId, AssetType type, String q, Pageable pageable) {
        boolean hasQ = q != null && !q.isBlank();
        Page<DigitalAsset> page;
        if (type != null && hasQ) {
            page = assetRepository.findByOwnerIdAndTypeAndTitleLike(ownerId, type, q, pageable);
        } else if (type != null) {
            page = assetRepository.findByOwnerIdAndType(ownerId, type, pageable);
        } else if (hasQ) {
            page = assetRepository.findByOwnerIdAndTitleLike(ownerId, q, pageable);
        } else {
            page = assetRepository.findByOwnerId(ownerId, pageable);
        }
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public AssetResponse get(Long ownerId, Long assetId) {
        DigitalAsset asset = loadOwned(ownerId, assetId);
        return toResponse(asset);
    }

    public AssetResponse update(Long ownerId, Long assetId, AssetUpdateRequest request) {
        DigitalAsset asset = loadOwned(ownerId, assetId);
        String storedContent = null;
        boolean contentChanged = request.content() != null;
        if (contentChanged) {
            storedContent = encryptionService.encrypt(request.content());
        }
        asset.update(
                request.title(),
                request.type(),
                request.description(),
                storedContent,
                contentChanged && encryptionService.isEnabled(),
                request.externalRef(),
                request.sensitivityLevel()
        );
        return toResponse(asset);
    }

    public void delete(Long ownerId, Long assetId) {
        DigitalAsset asset = loadOwned(ownerId, assetId);
        assetRepository.delete(asset);
    }

    private DigitalAsset loadOwned(Long ownerId, Long assetId) {
        DigitalAsset asset = assetRepository.findById(assetId)
                .orElseThrow(AssetNotFoundException::new);
        asset.assertOwnedBy(ownerId);
        return asset;
    }

    private AssetResponse toResponse(DigitalAsset asset) {
        String plain = asset.getContent();
        if (asset.isContentEncrypted() && plain != null) {
            plain = encryptionService.decrypt(plain);
        }
        return AssetResponse.from(asset, plain);
    }
}
