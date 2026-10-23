package com.nabin.bankingwallet.common.exception;

import org.springframework.http.HttpStatus;

public class AccountFrozenException extends BusinessException {

    public AccountFrozenException() {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "ACCOUNT_FROZEN",
                "Account is frozen"
        );
    }
}