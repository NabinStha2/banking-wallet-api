package com.nabin.bankingwallet.transaction;

import com.nabin.bankingwallet.account.AccountService;
import com.nabin.bankingwallet.common.util.PageUtils;
import com.nabin.bankingwallet.transaction.dto.TransactionFilter;
import com.nabin.bankingwallet.transaction.dto.response.PageResponse;
import com.nabin.bankingwallet.transaction.dto.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class TransactionQueryService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    /**
     * History of one of the caller's accounts.
     */
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> history(
            Long accountId,
            Long userId,
            TransactionFilter filter,
            Pageable pageable
                                                    ) {
        accountService.getOwned(accountId, userId);
        return search(accountId, filter, pageable);
    }

    /**
     * Used by the admin endpoint: no ownership restriction, the account filter is optional.
     */
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> searchAll(
            Long accountId,
            TransactionFilter filter,
            Pageable pageable
                                                      ) {
        return search(accountId, filter, pageable);
    }

    private PageResponse<TransactionResponse> search(
            Long accountId,
            TransactionFilter filter,
            Pageable pageable

                                                    ) {
        Specification<Transaction> spec = Specification
                .where(TransactionSpecifications.forAccount(accountId))
                .and(TransactionSpecifications.forFilter(filter));
        Page<Transaction> page = transactionRepository.findAll(spec,
                                                               PageUtils.buildPageable(pageable));
        return PageResponse.from(page.map(TransactionResponse::fromTransaction));
    }
}
