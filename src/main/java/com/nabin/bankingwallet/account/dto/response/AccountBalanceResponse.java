package com.nabin.bankingwallet.account.dto.response;

import java.math.BigDecimal;

public record AccountBalanceResponse(
        Long accountId,
        BigDecimal balance,
        String currency
) {
    public static AccountBalanceResponse fromAccount(AccountResponse account) {
        return new AccountBalanceResponse(account.id(), account.balance(), account.currency());
    }
}
