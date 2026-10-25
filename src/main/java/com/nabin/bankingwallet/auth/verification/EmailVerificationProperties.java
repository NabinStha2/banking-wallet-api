package com.nabin.bankingwallet.auth.verification;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.email-verification")
public record EmailVerificationProperties(
        URI pageUrl,
        String from,
        Duration tokenTtl,
        Duration resendCooldown
) {
}
