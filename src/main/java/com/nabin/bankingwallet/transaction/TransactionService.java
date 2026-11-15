package com.nabin.bankingwallet.transaction;

import com.nabin.bankingwallet.account.Account;
import com.nabin.bankingwallet.account.AccountRules;
import com.nabin.bankingwallet.account.AccountService;
import com.nabin.bankingwallet.audit.AuditService;
import com.nabin.bankingwallet.common.exception.InsufficientFundsException;
import com.nabin.bankingwallet.common.util.MoneyUtils;
import com.nabin.bankingwallet.idempotency.IdempotencyService;
import com.nabin.bankingwallet.idempotency.IdempotencyUtils;
import com.nabin.bankingwallet.transaction.dto.request.MoneyRequest;
import com.nabin.bankingwallet.transaction.dto.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final AccountRules accountRules;
    private final IdempotencyService idempotencyService;
    private final IdempotencyUtils idempotencyUtils;
    private final AuditService auditService;

    @Transactional
    public TransactionResponse deposit(Long userId, Long accountId, MoneyRequest moneyRequest,
                                       String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return executeDeposit(userId, accountId, moneyRequest);
        }

        return idempotencyService.execute(
                userId,
                idempotencyKey.trim(),
                idempotencyUtils.generateHash(moneyRequest),
                TransactionResponse.class,
                () -> executeDeposit(userId, accountId, moneyRequest));
    }

    @Transactional
    public TransactionResponse withdraw(Long userId, Long accountId, MoneyRequest moneyRequest,
                                        String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return executeWithdraw(userId, accountId, moneyRequest);
        }

        return idempotencyService.execute(
                userId,
                idempotencyKey.trim(),
                idempotencyUtils.generateHash(moneyRequest),
                TransactionResponse.class,
                () -> executeWithdraw(userId, accountId, moneyRequest));
    }

    /** The actual deposit logic. Runs inside the idempotency transaction. */
    @Transactional
    public TransactionResponse executeDeposit(Long userId, Long accountId, MoneyRequest moneyRequest) {
        BigDecimal amount = MoneyUtils.normalize(moneyRequest.amount());
        Account account = accountService.getOwnedForUpdate(accountId, userId);
        accountRules.assertActive(account);

        account.setBalance(account
                                   .getBalance()
                                   .add(amount));

        Transaction entry = Transaction.of(account, TransactionType.DEPOSIT, amount, null, UUID
                .randomUUID()
                .toString(), moneyRequest.description());

        TransactionResponse response = TransactionResponse.fromTransaction(transactionRepository.save(entry));

        // Best-effort audit
        auditService.record(userId, "DEPOSIT", "Transaction", account.getId(),
                            "amount=" + amount + ",reference=" + response.id());

        return response;
    }

    /** The actual withdrawal logic. Runs inside the idempotency transaction. */
    @Transactional
    public TransactionResponse executeWithdraw(Long userId, Long accountId, MoneyRequest moneyRequest) {
        BigDecimal amount = MoneyUtils.normalize(moneyRequest.amount());
        Account account = accountService.getOwnedForUpdate(accountId, userId);

        accountRules.assertActive(account);
        accountRules.assertSufficientFunds(account, amount);
        accountRules.assertDailyLimitNotExceeded(account, amount);

        account.setBalance(account
                                   .getBalance()
                                   .subtract(amount));

        Transaction entry = Transaction.of(account, TransactionType.WITHDRAWAL, amount, null, UUID
                .randomUUID()
                .toString(), moneyRequest.description());

        TransactionResponse response = TransactionResponse.fromTransaction(transactionRepository.save(entry));

        // Best-effort audit
        auditService.record(userId, "WITHDRAWAL", "Transaction", account.getId(),
                            "amount=" + amount + ",reference=" + response.id());

        return response;
    }
}