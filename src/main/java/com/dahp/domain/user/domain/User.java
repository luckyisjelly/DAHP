package com.dahp.domain.user.domain;

import com.dahp.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    private static final int DEFAULT_CHECK_IN_INTERVAL_DAYS = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "check_in_interval_days", nullable = false)
    private Integer checkInIntervalDays;

    @Column(name = "last_check_in_at")
    private LocalDateTime lastCheckInAt;

    @Column(name = "next_check_in_due_at")
    private LocalDateTime nextCheckInDueAt;

    @Builder
    private User(String email, String passwordHash, UserRole role, Integer checkInIntervalDays) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : UserRole.USER;
        this.checkInIntervalDays = checkInIntervalDays != null ? checkInIntervalDays : DEFAULT_CHECK_IN_INTERVAL_DAYS;
        this.nextCheckInDueAt = LocalDateTime.now().plusDays(this.checkInIntervalDays);
    }

    public void recordCheckIn() {
        this.lastCheckInAt = LocalDateTime.now();
        this.nextCheckInDueAt = this.lastCheckInAt.plusDays(this.checkInIntervalDays);
    }

    public void updateCheckInInterval(Integer days) {
        this.checkInIntervalDays = days;
        LocalDateTime base = this.lastCheckInAt != null ? this.lastCheckInAt : LocalDateTime.now();
        this.nextCheckInDueAt = base.plusDays(days);
    }

    public boolean isCheckInOverdue() {
        return nextCheckInDueAt != null && LocalDateTime.now().isAfter(nextCheckInDueAt);
    }
}
