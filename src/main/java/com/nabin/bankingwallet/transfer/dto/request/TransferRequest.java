package com.nabin.bankingwallet.transfer.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull
        Long fromAccountId,
        @NotBlank
        String toAccountNumber,
        @Size(max = 255,
                message = "Description must not exceed 255 characters")
        String description,
        @NotNull
        @DecimalMin(value = "0.01",
                message = "Amount must be greater than 0")
        @Digits(integer = 17,
                fraction = 2,
                message = "Amount must be a valid monetary value with up to 2 decimal places")
        BigDecimal amount) {
}
