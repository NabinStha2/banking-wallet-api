package com.nabin.bankingwallet.transaction;


import com.nabin.bankingwallet.security.UserPrincipal;
import com.nabin.bankingwallet.transaction.dto.TransactionFilter;
import com.nabin.bankingwallet.transaction.dto.response.PageResponse;
import com.nabin.bankingwallet.transaction.dto.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/accounts/{accountId}/transactions")
@RequiredArgsConstructor
public class TransactionHistoryController {

    private final TransactionQueryService transactionQueryService;


    @GetMapping
    public PageResponse<TransactionResponse> history(
            @AuthenticationPrincipal
            UserPrincipal principal,
            @PathVariable(name = "accountId")
            Long accountId,
            TransactionFilter filter,
            Pageable pageable
                                                    ) {
        return transactionQueryService.history(accountId, principal.getId(), filter, pageable);
    }
}
