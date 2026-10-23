package com.nabin.bankingwallet.transaction;

import com.nabin.bankingwallet.account.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false,
            length = 20)
    private TransactionType type;

    @Column(nullable = false,
            precision = 19,
            scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after",
            nullable = false,
            precision = 19,
            scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "counterparty_account_id")
    private Long counterpartyAccountId;

    @Column(name = "reference_id",
            nullable = false,
            length = 36)
    private String referenceId;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private Instant createdAt;

    /**
     * Call AFTER the account balance has been updated, so balanceAfter is correct.
     */
    public static Transaction of(
            Account account,
            TransactionType type,
            BigDecimal amount,
            Long counterpartyId,
            String referenceId,
            String description
                                ) {
        Transaction t = new Transaction();
        t.account = account;
        t.type = type;
        t.amount = amount;
        t.balanceAfter = account.getBalance();
        t.counterpartyAccountId = counterpartyId;
        t.referenceId = referenceId;
        t.description = description;
        return t;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}