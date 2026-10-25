package com.nabin.bankingwallet.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyEmailRequest(
        @NotBlank
        @Size(max = 64)
        String token
) {
}
