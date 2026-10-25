package com.nabin.bankingwallet.config;

import com.nabin.bankingwallet.auth.verification.EmailVerificationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(EmailVerificationProperties.class)
public class EmailVerificationConfiguration {

    @Bean
    Clock applicationClock() {
        return Clock.systemUTC();
    }
}
