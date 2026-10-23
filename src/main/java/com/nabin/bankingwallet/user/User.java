package com.nabin.bankingwallet.user;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,
            unique = true)
    private String email;

    @Column(name = "password_hash",
            nullable = false,
            length = 100)
    private String passwordHash;

    @Column(name = "full_name",
            nullable = false,
            length = 120)
    private String fullName;

    @Column(name = "user_role",
            length = 20)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    private Role userRole;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }


}
