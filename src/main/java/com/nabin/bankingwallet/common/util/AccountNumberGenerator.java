package com.nabin.bankingwallet.common.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AccountNumberGenerator {
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateAccountNumber() {
        StringBuilder sb = new StringBuilder("4000");

        /* 16 digits: the prefix 4000 followed by 12 random digits. */
        for (int i = 0; i < 12; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }
}
