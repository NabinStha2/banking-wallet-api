package com.nabin.bankingwallet.account;

import com.nabin.bankingwallet.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "accounts"
//        ,
//        indexes = {
//                @Index(name = "idx_account_user_id",
//                        columnList = "user_id")
//        },
//        uniqueConstraints = {
//                @UniqueConstraint(name = "uk_account_account_number",
//                        columnNames = "account_number")
//        }
)
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "account_number",
            nullable = false,
            unique = true,
            length = 20)
    private String accountNumber;

    @Column(nullable = false,
            length = 3)
    private String currency;

    @Column(nullable = false,
            precision = 19,
            scale = 2)
    private BigDecimal balance = new BigDecimal("0.00");

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false,
            length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "daily_limit",
            nullable = false,
            precision = 19,
            scale = 2)
    private BigDecimal dailyLimit = new BigDecimal("5000.00");

    @Version
    private Long version;

    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}