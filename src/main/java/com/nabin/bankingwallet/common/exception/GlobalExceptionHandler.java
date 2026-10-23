package com.nabin.bankingwallet.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiError> business(
            BusinessException ex,
            HttpServletRequest req
                                     ) {
        return build(
                ex.getStatus(),
                ex.getCode(),
                ex.getMessage(),
                req,
                List.of()
                    );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(
            MethodArgumentNotValidException ex,
            HttpServletRequest req
                                       ) {
        List<ApiError.FieldError> fields =
                ex
                        .getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(f -> new ApiError.FieldError(
                                f.getField(),
                                f.getDefaultMessage() != null
                                ? f.getDefaultMessage()
                                : "Invalid value"
                        ))
                        .toList();

        return build(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                "Request validation failed",
                req,
                fields
                    );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> constraint(
            ConstraintViolationException ex,
            HttpServletRequest req
                                       ) {
        List<ApiError.FieldError> fields =
                ex
                        .getConstraintViolations()
                        .stream()
                        .map(v -> new ApiError.FieldError(
                                v
                                        .getPropertyPath()
                                        .toString(),
                                v.getMessage()
                        ))
                        .toList();

        return build(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                "Request validation failed",
                req,
                fields
                    );
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    ResponseEntity<ApiError> unreadable(
            Exception ex,
            HttpServletRequest req
                                       ) {
        return build(
                HttpStatus.BAD_REQUEST,
                "MALFORMED_REQUEST",
                "Malformed or invalid request",
                req,
                List.of()
                    );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    ResponseEntity<ApiError> header(
            MissingRequestHeaderException ex,
            HttpServletRequest req
                                   ) {
        return build(
                HttpStatus.BAD_REQUEST,
                "MISSING_HEADER",
                "Missing header: " + ex.getHeaderName(),
                req,
                List.of()
                    );
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> auth(
            AuthenticationException ex,
            HttpServletRequest req
                                 ) {
        return build(
                HttpStatus.UNAUTHORIZED,
                "INVALID_CREDENTIALS",
                "Invalid credentials",
                req,
                List.of()
                    );
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> denied(
            AccessDeniedException ex,
            HttpServletRequest req
                                   ) {
        return build(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                "You do not have permission",
                req,
                List.of()
                    );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> integrity(
            DataIntegrityViolationException ex,
            HttpServletRequest req
                                      ) {
        log.warn("Data integrity violation", ex);

        return build(
                HttpStatus.CONFLICT,
                "DATA_CONFLICT",
                "Request conflicts with existing data",
                req,
                List.of()
                    );
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    ResponseEntity<ApiError> lock(
            PessimisticLockingFailureException ex,
            HttpServletRequest req
                                 ) {
        log.warn("Lock failure", ex);

        return build(
                HttpStatus.CONFLICT,
                "CONCURRENT_UPDATE",
                "Account is busy, please retry",
                req,
                List.of()
                    );
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(
            Exception ex,
            HttpServletRequest req
                                       ) {
        log.error("Unexpected error", ex);

        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Something went wrong",
                req,
                List.of()
                    );
    }

    private ResponseEntity<ApiError> build(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest req,
            List<ApiError.FieldError> fields
                                          ) {
        ApiError body = new ApiError(
                UUID
                        .randomUUID()
                        .toString(),
                code,
                message,
                status.value(),
                status.name(),
                req.getRequestURI(),
                req.getMethod(),
                Instant.now(),
                fields
        );

        return ResponseEntity
                .status(status)
                .body(body);
    }
}
