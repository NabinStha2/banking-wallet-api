package com.nabin.bankingwallet.idempotency;

import com.nabin.bankingwallet.common.exception.BusinessException;
import com.nabin.bankingwallet.common.exception.IdempotencyConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Owns the idempotency lifecycle for mutating requests.
 *
 * <p>Clients send an {@code Idempotency-Key} header. The service:
 * <ul>
 *   <li>hashes the request body so two different payloads can never share a key;</li>
 *   <li>returns the cached response when the same key+hash is replayed;</li>
 *   <li>rejects concurrent replays while a request is still {@code IN_PROGRESS};</li>
 *   <li>rolls the {@code IN_PROGRESS} marker away when the work fails, so the
 *       client can safely retry.</li>
 * </ul>
 *
 * <p>This class is {@code @Transactional} with {@code REQUIRED} propagation, so a
 * caller that already runs inside a transaction (e.g. {@code TransferService})
 * joins it: the idempotency row and the business work live in one atomic unit.
 */
@Service
@Slf4j
public class IdempotencyService {

    private final IdempotencyRepository repository;
    private final IdempotencyUtils utils;

    public IdempotencyService(IdempotencyRepository repository, IdempotencyUtils utils) {
        this.repository = repository;
        this.utils = utils;
    }

    /**
     * Run {@code work} under the given idempotency key.
     *
     * <ul>
     *   <li><b>Replay with different body</b> — reject: the key is bound to another payload.</li>
     *   <li><b>Cache hit (COMPLETED)</b> — deserialize and return the stored response.</li>
     *   <li><b>Concurrent replay (IN_PROGRESS)</b> — reject until the first request finishes.</li>
     *   <li><b>New key</b> — mark {@code IN_PROGRESS}, run the work, then store the response.</li>
     * </ul>
     * On failure the {@code IN_PROGRESS} row is dropped so the client can retry.
     */
    @Transactional
    public <T> T execute(Long userId, String keyValue, String requestHash,
                         Class<T> responseClass, IdempotentWork<T> work) {
        Optional<IdempotencyKey> existing = repository.findByUserIdAndKeyValue(userId, keyValue);

        if (existing.isPresent()) {
            IdempotencyKey key = existing.get();
            if (!key.getRequestHash().equals(requestHash)) {
                throw new IdempotencyConflictException("HASH_MISMATCH",
                        "Idempotency key already used for a different request");
            }
            if (key.getStatus() == IdempotencyStatus.COMPLETED) {
                log.debug("Idempotency cache hit: user={} key={}", userId, keyValue);
                return utils.deserialize(key.getResponseBody(), responseClass);
            }
            throw new IdempotencyConflictException("IN_PROGRESS",
                    "A request with this idempotency key is already in progress");
        }

        IdempotencyKey key = new IdempotencyKey();
        key.setUserId(userId);
        key.setKeyValue(keyValue);
        key.setRequestHash(requestHash);
        key.setStatus(IdempotencyStatus.IN_PROGRESS);
        try {
            repository.saveAndFlush(key);
        } catch (DataIntegrityViolationException e) {
            // Race condition: another thread inserted the same key between our
            // SELECT and INSERT. Re-read and apply normal logic.
            IdempotencyKey existingKey = repository.findByUserIdAndKeyValue(userId, keyValue)
                    .orElseThrow(() -> new IllegalStateException("Key vanished after insert conflict"));
            if (!existingKey.getRequestHash().equals(requestHash)) {
                throw new IdempotencyConflictException("HASH_MISMATCH",
                        "Idempotency key already used for a different request");
            }
            if (existingKey.getStatus() == IdempotencyStatus.COMPLETED) {
                log.debug("Idempotency cache hit (race): user={} key={}", userId, keyValue);
                return utils.deserialize(existingKey.getResponseBody(), responseClass);
            }
            throw new IdempotencyConflictException("IN_PROGRESS",
                    "A request with this idempotency key is already in progress");
        }

        try {
            T result = work.execute();
            key.setStatus(IdempotencyStatus.COMPLETED);
            key.setResponseStatus(200);
            key.setResponseBody(utils.serialize(result));
            repository.save(key);
            return result;
        } catch (BusinessException e) {
            // Business rule violation — client can fix request and retry with same key
            deleteKey(key);
            throw e;
        } catch (Exception e) {
            // Infrastructure failure (DB, network, serialization) — DO NOT delete key.
            // Leave it IN_PROGRESS so the failure is visible and doesn't consume the key.
            throw e;
        }
    }

    /**
     * Deletes the key in a NEW transaction so it commits even if the caller's
     * transaction rolls back. This is essential for cleaning up IN_PROGRESS
     * markers on business failures without leaving stale rows.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteKey(IdempotencyKey key) {
        repository.delete(key);
    }

    /** The unit of business work that must run exactly once per idempotency key. */
    @FunctionalInterface
    public interface IdempotentWork<T> {
        T execute();
    }
}