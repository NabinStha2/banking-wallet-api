package com.nabin.bankingwallet.common.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates 16-digit account numbers (prefix 4000 + 12 random digits).
 *
 * <p>The generator itself has no collision awareness — callers must retry on
 * collision. The bounded attempt count keeps a pathological run from looping
 * forever if the number space were ever exhausted.
 */
@Component
public class AccountNumberGenerator {

    /** Maximum number of attempts before giving up. The 12 random digits
     *  give 10^12 combinations, so a collision is astronomically unlikely;
     *  this is purely a safety bound. */
    private static final int MAX_ATTEMPTS = 100;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * @return a fresh 16-digit account number. Never returns null.
     */
    public String generateAccountNumber() {
        StringBuilder sb = new StringBuilder("4000");
        for (int i = 0; i < 12; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * @return a fresh account number, guaranteed unique within the supplied
     *         repository. Throws after {@link #MAX_ATTEMPTS} failed attempts.
     */
    public String generateUniqueNumber(java.util.function.Predicate<String> isTaken) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String number = generateAccountNumber();
            if (!isTaken.test(number)) {
                return number;
            }
        }
        throw new IllegalStateException(
                "Failed to generate a unique account number after " + MAX_ATTEMPTS + " attempts");
    }
}