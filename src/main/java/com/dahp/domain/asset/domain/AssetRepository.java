package com.dahp.domain.asset.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssetRepository extends JpaRepository<DigitalAsset, Long> {

    Page<DigitalAsset> findByOwnerId(Long ownerId, Pageable pageable);

    Page<DigitalAsset> findByOwnerIdAndType(Long ownerId, AssetType type, Pageable pageable);

    @Query("""
            SELECT a FROM DigitalAsset a
            WHERE a.ownerId = :ownerId
              AND LOWER(a.title) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<DigitalAsset> findByOwnerIdAndTitleLike(@Param("ownerId") Long ownerId,
                                                 @Param("q") String q,
                                                 Pageable pageable);

    @Query("""
            SELECT a FROM DigitalAsset a
            WHERE a.ownerId = :ownerId
              AND a.type = :type
              AND LOWER(a.title) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<DigitalAsset> findByOwnerIdAndTypeAndTitleLike(@Param("ownerId") Long ownerId,
                                                       @Param("type") AssetType type,
                                                       @Param("q") String q,
                                                       Pageable pageable);
}
