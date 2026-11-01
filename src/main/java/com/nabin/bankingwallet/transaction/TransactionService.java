package com.nabin.bankingwallet.transaction;

import com.nabin.bankingwallet.account.Account;
import com.nabin.bankingwallet.account.AccountRules;
import com.nabin.bankingwallet.account.AccountService;
import com.nabin.bankingwallet.common.exception.InsufficientFundsException;
import com.nabin.bankingwallet.common.util.MoneyUtils;
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

    @Transactional
    public TransactionResponse deposit(Long userId, Long accountId, MoneyRequest moneyRequest) {
        BigDecimal amount = MoneyUtils.normalize(moneyRequest.amount());
        Account account = accountService.getOwnedForUpdate(accountId, userId);
        accountRules.assertActive(account);

        account.setBalance(account
                                   .getBalance()
                                   .add(amount));

        Transaction entry = Transaction.of(account, TransactionType.DEPOSIT, amount, null, UUID
                .randomUUID()
                .toString(), moneyRequest.description());

        return TransactionResponse.fromTransaction(transactionRepository.save(entry));
    }

    @Transactional
    public TransactionResponse withdraw(Long userId, Long accountId, MoneyRequest moneyRequest) {
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

        return TransactionResponse.fromTransaction(transactionRepository.save(entry));
    }
}
