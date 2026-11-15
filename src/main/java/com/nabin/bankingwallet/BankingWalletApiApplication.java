package com.nabin.bankingwallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables {@code @Scheduled} tasks (e.g. idempotency key cleanup).
 */
@SpringBootApplication
@EnableScheduling
public class BankingWalletApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingWalletApiApplication.class, args);
    }

}
