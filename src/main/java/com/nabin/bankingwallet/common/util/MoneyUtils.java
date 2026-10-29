package com.nabin.bankingwallet.common.util;

import com.nabin.bankingwallet.common.exception.InvalidAmountException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {

    // •	final class + private constructor: a utility class. It only holds static helper methods and cannot be instantiated or extended.
    private MoneyUtils() {
    }

    public static BigDecimal normalize(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }

        if (amount
                    .stripTrailingZeros()
                    .scale() > 2) {
            throw new InvalidAmountException("Amount must have at most two decimal places");
        }

        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }
}
