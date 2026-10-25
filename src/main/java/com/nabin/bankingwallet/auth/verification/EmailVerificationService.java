package com.nabin.bankingwallet.auth.verification;

import com.nabin.bankingwallet.common.exception.InvalidVerificationTokenException;
import com.nabin.bankingwallet.common.exception.ResendCooldownException;
import com.nabin.bankingwallet.user.User;
import com.nabin.bankingwallet.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private static final int MAX_TOKEN_LENGTH = 64;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final VerificationEmailSender emailSender;
    private final EmailVerificationProperties properties;
    private final Clock clock;

    static String generate() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    static String hashToken(String rawToken) {
        try {
            byte[] hash = MessageDigest
                    .getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat
                    .of()
                    .formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    @Transactional
    public void sendInitialVerification(User user) {
        issueAndSend(user, new EmailVerificationToken(), clock.instant());
    }

    @Transactional
    public void resendVerification(String email) {
        String normalizedEmail = email
                .trim()
                .toLowerCase(Locale.ROOT);
        Optional<User> userResult = userRepository.findByEmailForUpdate(normalizedEmail);
        if (userResult.isEmpty()) {
            return;
        }

        User user = userResult.get();
        if (user.isEmailVerified()) {
            return;
        }

        Instant now = clock.instant();
        Optional<EmailVerificationToken> currentToken = tokenRepository.findByUserIdForUpdate(
                user.getId());

        if (currentToken.isPresent()) {
            Instant cooldownEndsAt = currentToken
                    .get()
                    .getCreatedAt()
                    .plus(properties.resendCooldown());
            if (cooldownEndsAt.isAfter(now)) {
                throw new ResendCooldownException();
            }
        }

        issueAndSend(user, currentToken.orElseGet(EmailVerificationToken::new), now);
    }

    @Transactional
    public void verify(String rawToken) {
        if (rawToken == null || rawToken.isBlank() || rawToken.length() > MAX_TOKEN_LENGTH) {
            throw new InvalidVerificationTokenException();
        }

        String tokenHash = hashToken(rawToken);
        EmailVerificationToken token = tokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(InvalidVerificationTokenException::new);

        Instant now = clock.instant();
        if (token.getUsedAt() != null || !token
                .getExpiresAt()
                .isAfter(now)) {
            throw new InvalidVerificationTokenException();
        }

        token
                .getUser()
                .setEmailVerified(true);
        token.setUsedAt(now);
        tokenRepository.save(token);
    }

    private void issueAndSend(User user, EmailVerificationToken token, Instant now) {
        String rawToken = generate();

        token.setUser(user);
        token.setTokenHash(hashToken(rawToken));
        token.setExpiresAt(now.plus(properties.tokenTtl()));
        token.setCreatedAt(now);
        token.setUsedAt(null);

        tokenRepository.saveAndFlush(token);
        emailSender.send(user.getEmail(), rawToken);
    }
}
