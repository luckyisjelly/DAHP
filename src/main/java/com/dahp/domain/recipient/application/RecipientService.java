package com.dahp.domain.recipient.application;

import com.dahp.domain.recipient.controller.dto.RecipientCreateRequest;
import com.dahp.domain.recipient.controller.dto.RecipientResponse;
import com.dahp.domain.recipient.controller.dto.RecipientUpdateRequest;
import com.dahp.domain.recipient.domain.Recipient;
import com.dahp.domain.recipient.domain.RecipientRepository;
import com.dahp.domain.recipient.exception.RecipientNotFoundException;
import com.dahp.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RecipientService {

    private final RecipientRepository recipientRepository;

    public RecipientResponse create(Long ownerId, RecipientCreateRequest request) {
        Recipient recipient = Recipient.builder()
                .ownerId(ownerId)
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .relationship(request.relationship())
                .memo(request.memo())
                .build();
        return RecipientResponse.from(recipientRepository.save(recipient));
    }

    @Transactional(readOnly = true)
    public PageResponse<RecipientResponse> list(Long ownerId, String q, Pageable pageable) {
        boolean hasQ = q != null && !q.isBlank();
        Page<Recipient> page = hasQ
                ? recipientRepository.findByOwnerIdAndNameLike(ownerId, q, pageable)
                : recipientRepository.findByOwnerId(ownerId, pageable);
        return PageResponse.from(page.map(RecipientResponse::from));
    }

    @Transactional(readOnly = true)
    public RecipientResponse get(Long ownerId, Long recipientId) {
        return RecipientResponse.from(loadOwned(ownerId, recipientId));
    }

    public RecipientResponse update(Long ownerId, Long recipientId, RecipientUpdateRequest request) {
        Recipient recipient = loadOwned(ownerId, recipientId);
        recipient.update(
                request.name(),
                request.email(),
                request.phone(),
                request.relationship(),
                request.memo()
        );
        return RecipientResponse.from(recipient);
    }

    public void delete(Long ownerId, Long recipientId) {
        Recipient recipient = loadOwned(ownerId, recipientId);
        recipientRepository.delete(recipient);
    }

    private Recipient loadOwned(Long ownerId, Long recipientId) {
        Recipient recipient = recipientRepository.findById(recipientId)
                .orElseThrow(RecipientNotFoundException::new);
        recipient.assertOwnedBy(ownerId);
        return recipient;
    }
}
