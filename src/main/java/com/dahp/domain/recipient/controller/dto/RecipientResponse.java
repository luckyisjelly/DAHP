package com.dahp.domain.recipient.controller.dto;

import com.dahp.domain.recipient.domain.Recipient;

import java.time.LocalDateTime;

public record RecipientResponse(
        Long id,
        String name,
        String email,
        String phone,
        String relationship,
        String memo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static RecipientResponse from(Recipient r) {
        return new RecipientResponse(
                r.getId(),
                r.getName(),
                r.getEmail(),
                r.getPhone(),
                r.getRelationship(),
                r.getMemo(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
