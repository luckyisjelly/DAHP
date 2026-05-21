package com.dahp.domain.handover.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HandoverEventRepository extends JpaRepository<HandoverEvent, Long> {

    Optional<HandoverEvent> findByAccessTokenHash(String accessTokenHash);

    Page<HandoverEvent> findByOwnerId(Long ownerId, Pageable pageable);

    Page<HandoverEvent> findByOwnerIdAndStatus(Long ownerId, HandoverEventStatus status, Pageable pageable);

    List<HandoverEvent> findByRuleId(Long ruleId);

    /**
     * 만료 후보: 활성 상태(PENDING/NOTIFIED) + expires_at가 지난 이벤트.
     * 스케줄러가 일괄 EXPIRED 전이 시 사용.
     */
    List<HandoverEvent> findAllByStatusInAndExpiresAtBefore(Collection<HandoverEventStatus> statuses,
                                                            LocalDateTime threshold);
}
