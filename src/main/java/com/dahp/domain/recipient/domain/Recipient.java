package com.dahp.domain.recipient.domain;

import com.dahp.domain.recipient.exception.RecipientAccessDeniedException;
import com.dahp.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "recipients",
        indexes = @Index(name = "idx_recipient_owner", columnList = "owner_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recipient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 50)
    private String relationship;

    @Column(length = 500)
    private String memo;

    @Builder
    private Recipient(Long ownerId,
                      String name,
                      String email,
                      String phone,
                      String relationship,
                      String memo) {
        this.ownerId = ownerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.relationship = relationship;
        this.memo = memo;
    }

    public void update(String name,
                       String email,
                       String phone,
                       String relationship,
                       String memo) {
        if (name != null) this.name = name;
        if (email != null) this.email = email;
        if (phone != null) this.phone = phone;
        if (relationship != null) this.relationship = relationship;
        if (memo != null) this.memo = memo;
    }

    public void assertOwnedBy(Long userId) {
        if (!this.ownerId.equals(userId)) {
            throw new RecipientAccessDeniedException();
        }
    }
}
