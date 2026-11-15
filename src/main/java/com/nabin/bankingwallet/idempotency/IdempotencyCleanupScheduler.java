package com.nabin.bankingwallet.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Periodically purges stale idempotency keys.
 *
 * <p>Keys older than 30 days are deleted. IN_PROGRESS keys older than 1 hour
 * (likely abandoned) are also deleted so clients can retry.
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class IdempotencyCleanupScheduler {

    private final IdempotencyRepository repository;

    /**
     * Runs daily at 03:00 UTC. Deletes COMPLETED keys older than 30 days
     * and IN_PROGRESS keys older than 1 hour.
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "UTC")
    @Transactional
    public void purgeStaleKeys() {
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

        // Purge old completed keys
        long completed = repository.deleteCompletedBefore(thirtyDaysAgo);
        // Purge abandoned in-progress keys
        long inProgress = repository.deleteInProgressBefore(oneHourAgo);

        if (completed > 0 || inProgress > 0) {
            log.info("Idempotency purge: deleted {} completed, {} in-progress keys",
                     completed, inProgress);
        }
    }
}