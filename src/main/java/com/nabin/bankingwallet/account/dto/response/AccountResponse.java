package com.nabin.bankingwallet.account.dto.response;

import com.nabin.bankingwallet.account.Account;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String currency,
        String accountNumber,
        BigDecimal balance,
        String status,
        BigDecimal dailyLimit) {

    public static AccountResponse fromAccount(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getCurrency(),
                account.getAccountNumber(),
                account.getBalance(),
                account
                        .getStatus()
                        .name(),
                account.getDailyLimit()
        );
    }
}
