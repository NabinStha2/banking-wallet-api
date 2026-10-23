package com.nabin.bankingwallet.common.exception;

import java.time.Instant;
import java.util.List;

public record ApiError(
        String guid,         // unique trace ID
        String errorCode,    // e.g., "PRODUCT_NOT_FOUND"
        String message,      // user-friendly message
        int statusCode,  // e.g., 404
        String statusName,   // e.g., "NOT_FOUND"
        String path,         // request path
        String method,       // HTTP method
        Instant timestamp,
        List<FieldError> validationErrors) // optional, for validation errors
{

    public static ApiError of(String guid, String errorCode, String message, int statusCode,
                              String statusName, String path, String method) {
        return new ApiError(guid, errorCode, message, statusCode, statusName, path, method,
                            Instant.now(), List.of());
    }

    public record FieldError(String field, String message) {
    }

}