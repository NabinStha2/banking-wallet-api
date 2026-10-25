package com.nabin.bankingwallet.common.exception;

import org.springframework.http.HttpStatus;

public class ResendCooldownException extends BusinessException {

    public ResendCooldownException() {
        super(
                HttpStatus.TOO_MANY_REQUESTS,
                "VERIFICATION_RESEND_COOLDOWN",
                "Please wait before requesting another verification email"
             );
    }
}
