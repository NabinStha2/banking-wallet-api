package com.nabin.bankingwallet.account;

import com.nabin.bankingwallet.common.exception.AccountFrozenException;
import com.nabin.bankingwallet.common.exception.DailyLimitExceededException;
import com.nabin.bankingwallet.common.exception.InsufficientFundsException;
import com.nabin.bankingwallet.transaction.TransactionRepository;
import com.nabin.bankingwallet.transaction.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AccountRules {
    private final Clock clock;
    private final TransactionRepository transactionRepository;

    public void assertActive(Account account) {
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException();
        }
    }

    public void assertSufficientFunds(Account account, BigDecimal amount) {
        if (amount.compareTo(account.getBalance()) > 0) {
            throw new InsufficientFundsException();
        }
    }

    public void assertDailyLimitNotExceeded(Account account, BigDecimal amount) {
        Instant startOfDay = LocalDate
                .now(clock)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        BigDecimal used = transactionRepository.sumOutgoingSince(account.getId(), List.of(
                TransactionType.DEPOSIT, TransactionType.WITHDRAWAL,
                TransactionType.TRANSFER_IN, TransactionType.TRANSFER_OUT), startOfDay);

        if (used == null) {
            used = BigDecimal.ZERO;
        }

        if (used
                    .add(amount)
                    .compareTo(account.getDailyLimit()) > 0) {
            throw new DailyLimitExceededException();
        }
    }
}