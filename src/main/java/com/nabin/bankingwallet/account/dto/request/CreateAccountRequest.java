package com.nabin.bankingwallet.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateAccountRequest(
        @NotBlank
        @Pattern(regexp = "USD|EUR|GBP|AUD",
                message = "must be one of USD, EUR, GBP, AUD")
        String currency
) {
}
