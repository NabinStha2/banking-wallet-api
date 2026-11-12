package com.nabin.bankingwallet.transfer.dto.response;

import java.math.BigDecimal;

public record TransferResponse(
        String referenceId,
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount,
        BigDecimal balanceAfterTransfer,
        String status
) {
}
