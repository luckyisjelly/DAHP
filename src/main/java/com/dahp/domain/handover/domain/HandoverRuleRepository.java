package com.dahp.domain.handover.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HandoverRuleRepository extends JpaRepository<HandoverRule, Long> {

    Page<HandoverRule> findByOwnerId(Long ownerId, Pageable pageable);

    Page<HandoverRule> findByOwnerIdAndStatus(Long ownerId, HandoverRuleStatus status, Pageable pageable);

    List<HandoverRule> findAllByStatus(HandoverRuleStatus status);
}
