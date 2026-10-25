package com.nabin.bankingwallet.auth.verification;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from EmailVerificationToken token where token.tokenHash = :tokenHash")
    Optional<EmailVerificationToken> findByTokenHashForUpdate(
            @Param("tokenHash")
            String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from EmailVerificationToken token where token.user.id = :userId")
    Optional<EmailVerificationToken> findByUserIdForUpdate(
            @Param("userId")
            Long userId);
}
