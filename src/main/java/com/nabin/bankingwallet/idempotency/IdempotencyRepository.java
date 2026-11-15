package com.nabin.bankingwallet.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, Long> {
    Optional<IdempotencyKey> findByUserIdAndKeyValue(Long userId, String keyValue);

    @Modifying
    @Query("delete from IdempotencyKey k where k.status = 'COMPLETED' and k.createdAt < :cutoff")
    long deleteCompletedBefore(@Param("cutoff") Instant cutoff);

    @Modifying
    @Query("delete from IdempotencyKey k where k.status = 'IN_PROGRESS' and k.createdAt < :cutoff")
    long deleteInProgressBefore(@Param("cutoff") Instant cutoff);
}