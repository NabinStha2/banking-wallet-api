package com.nabin.bankingwallet.common.exception;

import org.springframework.http.HttpStatus;

public class DailyLimitExceededException extends BusinessException {

    public DailyLimitExceededException() {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "DAILY_LIMIT_EXCEEDED",
                "Daily outgoing limit exceeded"
        );
    }
}