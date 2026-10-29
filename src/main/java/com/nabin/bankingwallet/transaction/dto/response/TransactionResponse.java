package com.nabin.bankingwallet.transaction.dto.response;

import com.nabin.bankingwallet.transaction.Transaction;
import com.nabin.bankingwallet.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        Long accountId,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Long counterpartyAccountId,
        String referenceId,
        String description,
        Instant createdAt
) {
    public static TransactionResponse fromTransaction(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction
                        .getAccount()
                        .getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getCounterpartyAccountId(),
                transaction.getReferenceId(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}
