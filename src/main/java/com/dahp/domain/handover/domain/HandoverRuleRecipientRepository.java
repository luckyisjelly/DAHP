package com.dahp.domain.handover.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HandoverRuleRecipientRepository extends JpaRepository<HandoverRuleRecipient, Long> {

    List<HandoverRuleRecipient> findByRuleId(Long ruleId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM HandoverRuleRecipient hrr WHERE hrr.ruleId = :ruleId")
    void deleteByRuleId(@Param("ruleId") Long ruleId);
}
