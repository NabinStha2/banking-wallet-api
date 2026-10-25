package com.nabin.bankingwallet.auth.verification;


import com.nabin.bankingwallet.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "email_verification_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_email_verification_token_hash", columnNames = "token_hash"),
                @UniqueConstraint(name = "uk_email_verification_token_user", columnNames = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class EmailVerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(name = "user_id",
            nullable = false)
    private User user;

    @Column(name = "token_hash",
            nullable = false,
            length = 64)
    private String tokenHash;

    @Column(name = "expires_at",
            nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private Instant createdAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
