package com.nabin.bankingwallet.transaction;

import com.nabin.bankingwallet.account.Account;
import com.nabin.bankingwallet.account.AccountService;
import com.nabin.bankingwallet.common.exception.BusinessException;
import com.nabin.bankingwallet.transaction.dto.response.StatementResponse;
import com.nabin.bankingwallet.transaction.dto.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatementService {

    public static final long
            MAX_RANGE_DAYS = 366;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;


    @Transactional(readOnly = true)
    public StatementResponse getStatement(Long userId, Long accountId, LocalDate from,
                                          LocalDate to) {

        Account account = accountService.getOwned(accountId, userId);

        validateDateRange(from, to);

        Instant fromInstant = from
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        Instant toInstant = to
                .plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        BigDecimal openingBalance = transactionRepository.sumNetBefore(account.getId(),
                                                                       fromInstant);

        List<Transaction> entries = transactionRepository.findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAscIdAsc(
                account.getId(), fromInstant, toInstant);

        BigDecimal totalCredits = BigDecimal.ZERO;
        BigDecimal totalDebits = BigDecimal.ZERO;

        for (Transaction tx : entries) {
            if (tx.getType() == TransactionType.DEPOSIT ||
                tx.getType() == TransactionType.TRANSFER_IN) {
                totalCredits = totalCredits.add(tx.getAmount());
            } else {
                totalDebits = totalDebits.add(tx.getAmount());
            }
        }

        BigDecimal closingBalance = openingBalance
                .add(totalCredits)
                .subtract(totalDebits);

        List<TransactionResponse> entryDtos = entries
                .stream()
                .map(TransactionResponse::fromTransaction)
                .toList();

        return new StatementResponse(account.getAccountNumber(), account.getCurrency(), from, to,
                                     openingBalance, totalCredits, totalDebits, closingBalance,
                                     entryDtos);

    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "MISSING_DATES",
                                        "Both 'from' and 'to' dates are required.");
        }

        if (from.isAfter(to)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE",
                                        "The 'from' date must be before the 'to' date.");
        }

        long daysBetween = ChronoUnit.DAYS.between(from, to);
        if (daysBetween > MAX_RANGE_DAYS) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "DATE_RANGE_TOO_LARGE",
                                        "Date range cannot exceed " + MAX_RANGE_DAYS + " days");
        }
    }
}
