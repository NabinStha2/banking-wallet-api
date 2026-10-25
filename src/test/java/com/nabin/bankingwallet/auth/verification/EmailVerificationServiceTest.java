package com.nabin.bankingwallet.auth.verification;

import com.nabin.bankingwallet.common.exception.InvalidVerificationTokenException;
import com.nabin.bankingwallet.user.User;
import com.nabin.bankingwallet.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final String RAW_TOKEN = "a-valid-verification-token";

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationEmailSender emailSender;

    private EmailVerificationService verificationService;

    @BeforeEach
    void setUp() {
        EmailVerificationProperties properties = new EmailVerificationProperties(
                URI.create("http://localhost:8080/verify-email.html"),
                "no-reply@example.test",
                Duration.ofHours(24),
                Duration.ofMinutes(1)
        );
        verificationService = new EmailVerificationService(
                tokenRepository,
                userRepository,
                emailSender,
                properties,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void verifiesValidTokenAndConsumesIt() {
        User user = new User();
        user.setEmailVerified(false);
        EmailVerificationToken token = tokenFor(user, NOW.plusSeconds(60), null);
        when(tokenRepository.findByTokenHashForUpdate(EmailVerificationService.hashToken(RAW_TOKEN)))
                .thenReturn(Optional.of(token));

        verificationService.verify(RAW_TOKEN);

        assertTrue(user.isEmailVerified());
        assertEquals(NOW, token.getUsedAt());
        verify(tokenRepository).save(token);
    }

    @Test
    void rejectsExpiredToken() {
        EmailVerificationToken token = tokenFor(new User(), NOW, null);
        when(tokenRepository.findByTokenHashForUpdate(EmailVerificationService.hashToken(RAW_TOKEN)))
                .thenReturn(Optional.of(token));

        assertThrows(InvalidVerificationTokenException.class,
                     () -> verificationService.verify(RAW_TOKEN));
        assertFalse(token.getUser().isEmailVerified());
    }

    @Test
    void rejectsReusedToken() {
        EmailVerificationToken token = tokenFor(new User(), NOW.plusSeconds(60), NOW.minusSeconds(1));
        when(tokenRepository.findByTokenHashForUpdate(EmailVerificationService.hashToken(RAW_TOKEN)))
                .thenReturn(Optional.of(token));

        assertThrows(InvalidVerificationTokenException.class,
                () -> verificationService.verify(RAW_TOKEN));
        assertFalse(token.getUser().isEmailVerified());
    }

    @Test
    void rejectsUnknownToken() {
        when(tokenRepository.findByTokenHashForUpdate(EmailVerificationService.hashToken(RAW_TOKEN)))
                .thenReturn(Optional.empty());

        assertThrows(InvalidVerificationTokenException.class,
                () -> verificationService.verify(RAW_TOKEN));
    }

    private EmailVerificationToken tokenFor(User user, Instant expiresAt, Instant usedAt) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setTokenHash(EmailVerificationService.hashToken(RAW_TOKEN));
        token.setExpiresAt(expiresAt);
        token.setCreatedAt(NOW.minusSeconds(10));
        token.setUsedAt(usedAt);
        return token;
    }
}
