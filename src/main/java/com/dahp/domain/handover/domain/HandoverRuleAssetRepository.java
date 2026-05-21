package com.dahp.domain.handover.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HandoverRuleAssetRepository extends JpaRepository<HandoverRuleAsset, Long> {

    List<HandoverRuleAsset> findByRuleId(Long ruleId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM HandoverRuleAsset hra WHERE hra.ruleId = :ruleId")
    void deleteByRuleId(@Param("ruleId") Long ruleId);
}
