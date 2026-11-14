package com.nabin.bankingwallet.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    /**
     * Expose a configured ObjectMapper as a bean so it can be injected into
     * components like IdempotencyUtils. This ensures serialization matches
     * the rest of the application (property naming, date formats, etc.).
     */
    @Bean
    ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
