package com.nabin.bankingwallet.common.exception;

import org.springframework.http.HttpStatus;

public class AccountFrozenException extends BusinessException {

    public AccountFrozenException() {
        super(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "ACCOUNT_FROZEN",
                "Account is frozen"
        );
    }
}