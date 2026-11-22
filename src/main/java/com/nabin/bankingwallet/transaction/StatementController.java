package com.nabin.bankingwallet.transaction;

import com.nabin.bankingwallet.security.UserPrincipal;
import com.nabin.bankingwallet.transaction.dto.response.StatementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/statement")
@RequiredArgsConstructor
public class StatementController {

    private final StatementService statementService;

    @GetMapping
    public StatementResponse statement(
            @AuthenticationPrincipal
            UserPrincipal principal,
            @PathVariable
            Long accountId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {
        return statementService.getStatement(principal.getId(), accountId, from, to);
    }
}
