package com.nabin.bankingwallet.transaction.dto;

import com.nabin.bankingwallet.transaction.TransactionType;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Optional query-string filters: ?type=DEPOSIT&minAmount=10&maxAmount=500&fromDate=2025-01-01&toDate=2025-01-31
 */
public record TransactionFilter(TransactionType transactionType, BigDecimal minAmount,
                                BigDecimal maxAmount,
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
}
