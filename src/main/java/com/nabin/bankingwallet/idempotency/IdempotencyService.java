package com.nabin.bankingwallet.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nabin.bankingwallet.common.exception.IdempotencyConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    private final IdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyRepository idempotencyRepository,
                              IdempotencyUtils idempotencyUtils) {
        this.idempotencyRepository = idempotencyRepository;
        this.objectMapper = idempotencyUtils.objectMapper();
    }

    @Transactional(readOnly = true)
    public Optional<IdempotencyKey> getExistingKey(Long userId, String keyValue) {
        return idempotencyRepository.findByUserIdAndKeyValue(userId, keyValue);
    }

    @Transactional
    public IdempotencyKey createInProgress(Long userId, String keyValue, String requestHash) {
        IdempotencyKey key = new IdempotencyKey();
        key.setUserId(userId);
        key.setKeyValue(keyValue);
        key.setRequestHash(requestHash);
        key.setStatus(IdempotencyStatus.IN_PROGRESS);
        return idempotencyRepository.save(key);
    }

    @Transactional
    public void complete(IdempotencyKey key, int responseStatus, String responseBody) {
        key.setStatus(IdempotencyStatus.COMPLETED);
        key.setResponseStatus(responseStatus);
        key.setResponseBody(responseBody);
        idempotencyRepository.save(key);
    }

    @Transactional
    public void deleteKey(Long userId, String keyValue) {
        idempotencyRepository
                .findByUserIdAndKeyValue(userId, keyValue)
                .ifPresent(idempotencyRepository::delete);
    }

    public boolean isHashValid(IdempotencyKey key, String requestHash) {
        return key
                .getRequestHash()
                .equals(requestHash);
    }

    @Transactional
    public <T> T execute(Long userId, String keyValue, String requestHash,
                         Class<T> responseClass, IdempotentWork<T> work) {
        Optional<IdempotencyKey> existing = idempotencyRepository.findByUserIdAndKeyValue(userId,
                                                                                          keyValue);

        if (existing.isPresent()) {
            IdempotencyKey key = existing.get();
            if (!isHashValid(key, requestHash)) {
                throw new IdempotencyConflictException("HASH_MISMATCH",
                                                       "Idempotency key already used for a different request");
            }
            if (key.getStatus() == IdempotencyStatus.COMPLETED) {
                log.debug("Idempotency cache hit: user={} key={}", userId, keyValue);
                return readCachedResponse(key, responseClass);
            }
            throw new IdempotencyConflictException("IN_PROGRESS",
                                                   "A request with this idempotency key is already in progress");
        }

        IdempotencyKey key = createInProgress(userId, keyValue, requestHash);
        try {
            T result = work.execute();
            complete(key, 200, objectMapper.writeValueAsString(result));
            return result;
        } catch (RuntimeException e) {
            // Drop the marker so the client can replay the same key after fixing the request.
            deleteKey(userId, keyValue);
            throw e;
        } catch (Exception e) {
            deleteKey(userId, keyValue);
            throw new RuntimeException("Idempotent work failed", e);
        }
    }

    private <T> T readCachedResponse(IdempotencyKey key, Class<T> responseClass) {
        try {
            return objectMapper.readValue(key.getResponseBody(), responseClass);
        } catch (Exception e) {
            log.error("Corrupt cached idempotency response for key={}", key.getKeyValue(), e);
            throw new RuntimeException("Failed to read cached idempotency response", e);
        }
    }

    /**
     * The unit of business work that must run exactly once per idempotency key.
     */
    @FunctionalInterface
    public interface IdempotentWork<T> {
        T execute();
    }
}