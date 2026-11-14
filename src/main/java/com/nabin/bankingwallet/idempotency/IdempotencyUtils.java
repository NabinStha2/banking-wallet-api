package com.nabin.bankingwallet.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Small, dependency-free helpers for the idempotency layer.
 *
 * <p>Uses Spring's configured {@link ObjectMapper} so serialization matches
 * the rest of the application (property naming, date formats, etc.).
 */
@Component
@RequiredArgsConstructor
public class IdempotencyUtils {

    private final ObjectMapper objectMapper;

    /**
     * Deterministic SHA-256 of the request body's JSON. Two requests with the
     * same key but different bodies therefore never match.
     */
    public String generateHash(Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to calculate request hash for idempotency", e);
        }
    }

    public String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize idempotency response", e);
        }
    }

    public <T> T deserialize(String body, Class<T> responseClass) {
        try {
            return objectMapper.readValue(body, responseClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read cached idempotency response", e);
        }
    }
}