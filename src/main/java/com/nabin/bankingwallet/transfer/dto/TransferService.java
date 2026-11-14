package com.nabin.bankingwallet.transfer.dto;

import com.nabin.bankingwallet.account.Account;
import com.nabin.bankingwallet.account.AccountRepository;
import com.nabin.bankingwallet.account.AccountRules;
import com.nabin.bankingwallet.common.exception.BusinessException;
import com.nabin.bankingwallet.common.exception.ResourceNotFoundException;
import com.nabin.bankingwallet.common.util.MoneyUtils;
import com.nabin.bankingwallet.idempotency.IdempotencyService;
import com.nabin.bankingwallet.idempotency.IdempotencyUtils;
import com.nabin.bankingwallet.transaction.Transaction;
import com.nabin.bankingwallet.transaction.TransactionRepository;
import com.nabin.bankingwallet.transaction.TransactionType;
import com.nabin.bankingwallet.transfer.dto.request.TransferRequest;
import com.nabin.bankingwallet.transfer.dto.response.TransferResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final AccountRules accountRules;
    private final TransactionRepository transactionRepository;
    private final IdempotencyService idempotencyService;
    private final IdempotencyUtils idempotencyUtils;

    /**
     * Runs the transfer under an idempotency key when the client supplies one.
     * Without a key the transfer executes once and no idempotency row is written.
     */
    @Transactional
    public TransferResponse transfer(Long userId, TransferRequest transferRequest, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return executeTransfer(userId, transferRequest);
        }

        String requestHash = idempotencyUtils.generateHash(transferRequest);
        return idempotencyService.execute(
                userId,
                idempotencyKey.trim(),
                requestHash,
                TransferResponse.class,
                () -> executeTransfer(userId, transferRequest));
    }

    /** The actual transfer logic. Runs inside the idempotency transaction. */
    @Transactional
    public TransferResponse executeTransfer(Long userId, TransferRequest transferRequest) {
        BigDecimal amount = MoneyUtils.normalize(transferRequest.amount());

        // Ownership / existence checks happen before locking, so we fail fast with a
        // 404 rather than acquiring a lock on an account that doesn't belong to the caller.
        Account fromAccount = accountRepository
                .findByIdAndUser_Id(transferRequest.fromAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Source account not found"));

        Account toAccount = accountRepository
                .findByAccountNumber(transferRequest.toAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Destination account not found"));

        // Lock in deterministic ID order. If A->B and B->A run concurrently, both
        // threads now acquire the same lock in the same order, so no deadlock.
        Account[] pair = lockBoth(fromAccount.getId(), toAccount.getId());

        Account from = pair[0];
        Account to = pair[1];

        if (fromAccount
                .getId()
                .equals(toAccount.getId())) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_CONTENT, "SELF_TRANSFER",
                    "Cannot transfer to the same account");
        }

        if (!from
                .getCurrency()
                .equals(to.getCurrency())) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_CONTENT, "CURRENCY_MISMATCH",
                    "Cannot transfer between accounts with different currencies");
        }

        accountRules.assertActive(from);
        accountRules.assertActive(to);
        accountRules.assertSufficientFunds(from, amount);
        accountRules.assertDailyLimitNotExceeded(from, amount);

        from.setBalance(from
                                .getBalance()
                                .subtract(amount));
        to.setBalance(to
                              .getBalance()
                              .add(amount));

        String reference = UUID
                .randomUUID()
                .toString();

        transactionRepository.save(
                Transaction.of(from, TransactionType.TRANSFER_OUT, amount,
                               to.getId(), reference, transferRequest.description()));
        transactionRepository.save(
                Transaction.of(to, TransactionType.TRANSFER_IN, amount,
                               from.getId(), reference, transferRequest.description()));

        return new TransferResponse(
                reference,
                from.getId(),
                to.getId(),
                amount,
                from.getBalance(),
                "Completed"
        );
    }

    /**
     * Acquires pessimistic write locks on both accounts, always in ascending ID
     * order, so concurrent transfers between the same two accounts cannot deadlock.
     *
     * @return [from, to] — the locked accounts mapped back to their original roles.
     */
    private Account[] lockBoth(Long id1, Long id2) {
        Long first = Math.min(id1, id2);
        Long second = Math.max(id1, id2);

        Account a = accountRepository
                .findByIdForUpdate(first)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found"));

        Account b = accountRepository
                .findByIdForUpdate(second)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found"));

        // Map the locked rows back to their original roles. Since the two IDs are
        // distinct (self-transfer is rejected above), exactly one of them equals
        // the lower lock key.
        Account from = id1.equals(first) ? a : b;
        Account to = id2.equals(first) ? a : b;
        return new Account[]{from, to};
    }
}