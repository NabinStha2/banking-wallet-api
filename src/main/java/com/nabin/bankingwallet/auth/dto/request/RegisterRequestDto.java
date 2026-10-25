package com.nabin.bankingwallet.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        @Size(max = 255,
                message = "Email must not exceed 255 characters")
        String email,

        @Size(
                min = 8,
                max = 100,
                message = "Password must be between 8 and 100 characters long"
        )
        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "Password must contain letters and digits"
        )
        String password,

        @NotBlank
        @Size(max = 120,
                message = "Full name must not exceed 120 characters")
        String fullName
) {
}
