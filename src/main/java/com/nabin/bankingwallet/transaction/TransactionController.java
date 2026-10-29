package com.nabin.bankingwallet.transaction;

import com.nabin.bankingwallet.security.UserPrincipal;
import com.nabin.bankingwallet.transaction.dto.request.MoneyRequest;
import com.nabin.bankingwallet.transaction.dto.response.TransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public TransactionResponse deposit(
            @AuthenticationPrincipal
            UserPrincipal principal,
            @PathVariable
            Long accountId,
            @Valid
            @RequestBody
            MoneyRequest moneyRequest) {
        return transactionService.deposit(principal.getId(), accountId, moneyRequest);
    }

    @PostMapping("/withdraw")
    public TransactionResponse withdraw(
            @AuthenticationPrincipal
            UserPrincipal principal,
            @PathVariable
            Long accountId,
            @Valid
            @RequestBody
            MoneyRequest moneyRequest) {
        return transactionService.withdraw(principal.getId(), accountId, moneyRequest);
    }
}
