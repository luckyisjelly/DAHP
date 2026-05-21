package com.dahp.domain.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * 체크인 만료가 지난 사용자 목록 (nextCheckInDueAt < now).
     * INACTIVITY_PERIOD 조건 평가에 사용.
     */
    List<User> findAllByNextCheckInDueAtBefore(LocalDateTime threshold);
}
