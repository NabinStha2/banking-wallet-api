package com.nabin.bankingwallet.transaction;

import com.nabin.bankingwallet.security.UserPrincipal;
import com.nabin.bankingwallet.transaction.dto.request.MoneyRequest;
import com.nabin.bankingwallet.transaction.dto.response.TransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public TransactionResponse deposit(
            @AuthenticationPrincipal
            UserPrincipal principal,
            @org.springframework.web.bind.annotation.PathVariable
            Long accountId,
            @Valid
            @RequestBody
            MoneyRequest moneyRequest,
            @RequestHeader(name = "Idempotency-Key", required = false)
            String idempotencyKey) {
        return transactionService.deposit(principal.getId(), accountId, moneyRequest, idempotencyKey);
    }

    @PostMapping("/withdraw")
    public TransactionResponse withdraw(
            @AuthenticationPrincipal
            UserPrincipal principal,
            @org.springframework.web.bind.annotation.PathVariable
            Long accountId,
            @Valid
            @RequestBody
            MoneyRequest moneyRequest,
            @RequestHeader(name = "Idempotency-Key", required = false)
            String idempotencyKey) {
        return transactionService.withdraw(principal.getId(), accountId, moneyRequest, idempotencyKey);
    }
}