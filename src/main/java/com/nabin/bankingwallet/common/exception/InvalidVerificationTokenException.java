package com.nabin.bankingwallet.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidVerificationTokenException extends BusinessException {

    public InvalidVerificationTokenException() {
        super(HttpStatus.BAD_REQUEST, "INVALID_VERIFICATION_TOKEN", "Verification token is invalid or expired");
    }
}
