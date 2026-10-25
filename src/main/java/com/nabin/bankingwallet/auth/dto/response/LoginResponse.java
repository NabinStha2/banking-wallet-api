package com.nabin.bankingwallet.auth.dto.response;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
