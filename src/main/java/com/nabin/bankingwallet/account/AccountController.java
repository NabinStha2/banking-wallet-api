package com.nabin.bankingwallet.account;

import com.nabin.bankingwallet.account.dto.request.CreateAccountRequest;
import com.nabin.bankingwallet.account.dto.response.AccountBalanceResponse;
import com.nabin.bankingwallet.account.dto.response.AccountResponse;
import com.nabin.bankingwallet.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal
            UserPrincipal principal,
            @Valid
            @RequestBody
            CreateAccountRequest createAccountRequest
                                                        ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accountService.createAccount(principal.getId(), createAccountRequest));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> mine(
            @AuthenticationPrincipal
            UserPrincipal principal
                                                     ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountService.listMine(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(
            @AuthenticationPrincipal
            UserPrincipal principal,
            @PathVariable
            Long id
                                                     ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(accountService.get(id, principal.getId()));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<AccountBalanceResponse> balance(
            @AuthenticationPrincipal
            UserPrincipal principal,
            @PathVariable
            Long id) {
        AccountResponse account = accountService.get(id, principal.getId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(AccountBalanceResponse.fromAccount(account));
    }

}
