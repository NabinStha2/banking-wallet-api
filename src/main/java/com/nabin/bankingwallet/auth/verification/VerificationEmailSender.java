package com.nabin.bankingwallet.auth.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class VerificationEmailSender {

    private final JavaMailSender javaMailSender;
    private final EmailVerificationProperties properties;

    public void send(String email, String rawToken) {
        String link = UriComponentsBuilder
                .fromUri(properties.pageUrl())
                .queryParam("token", rawToken)
                .build()
                .encode()
                .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.from());
        message.setTo(email);
        message.setSubject("Verify your Banking Wallet email");
        message.setText("Use this link to verify your email address:\n\n"
                        + link
                        + "\n\nThis link expires in "
                        + properties.tokenTtl()
                        + ". If you did not create this account, you can ignore this email.");
        javaMailSender.send(message);
    }
}
