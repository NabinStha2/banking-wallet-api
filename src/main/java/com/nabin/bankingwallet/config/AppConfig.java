package com.nabin.bankingwallet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {

    /**
     * A Clock bean makes "today" injectable, so daily-limit logic is easy to test.
     */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
