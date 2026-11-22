package com.nabin.bankingwallet.transaction.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StatementResponse(
        String accountNumber,
        String currency,
        LocalDate from,
        LocalDate to,
        BigDecimal openingBalance,
        BigDecimal totalCredits,
        BigDecimal totalDebits,
        BigDecimal closingBalance,
        List<TransactionResponse> entries
) {
}
