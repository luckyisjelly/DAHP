package com.dahp.domain.handover.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HandoverEventRepository extends JpaRepository<HandoverEvent, Long> {

    Optional<HandoverEvent> findByAccessTokenHash(String accessTokenHash);

    Page<HandoverEvent> findByOwnerId(Long ownerId, Pageable pageable);

    Page<HandoverEvent> findByOwnerIdAndStatus(Long ownerId, HandoverEventStatus status, Pageable pageable);

    List<HandoverEvent> findByRuleId(Long ruleId);
}
