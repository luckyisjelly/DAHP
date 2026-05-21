package com.dahp.domain.recipient.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {

    Page<Recipient> findByOwnerId(Long ownerId, Pageable pageable);

    @Query("""
            SELECT r FROM Recipient r
            WHERE r.ownerId = :ownerId
              AND LOWER(r.name) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<Recipient> findByOwnerIdAndNameLike(@Param("ownerId") Long ownerId,
                                             @Param("q") String q,
                                             Pageable pageable);

    List<Recipient> findAllByIdInAndOwnerId(Collection<Long> ids, Long ownerId);

    List<Recipient> findAllByIdIn(Collection<Long> ids);
}
