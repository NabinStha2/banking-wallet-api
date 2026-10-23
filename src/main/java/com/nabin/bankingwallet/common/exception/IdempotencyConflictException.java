package com.nabin.bankingwallet.common.exception;

import org.springframework.http.HttpStatus;

public class IdempotencyConflictException extends BusinessException {

    public IdempotencyConflictException(String code, String message) {
        super(HttpStatus.CONFLICT, code, message);
    }
}