package com.nabin.bankingwallet.transaction;

import com.nabin.bankingwallet.account.Account;
import com.nabin.bankingwallet.account.AccountService;
import com.nabin.bankingwallet.transaction.dto.response.StatementResponse;
import com.nabin.bankingwallet.transaction.dto.response.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatementServiceTest {
    @Mock
    private AccountService accountService;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private StatementService statementService;

    @BeforeEach
    void setUp() {
        statementService = new StatementService(accountService, transactionRepository);
    }

    @Test
    void getStatement_whenValidRequest_returnsCorrectBalances() {
        // Create a fake account
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("4000123456789012");
        account.setCurrency("USD");

        // Tell Mockito: when getOwned(1L, 1L) is called, return this account
        when(accountService.getOwned(1L, 1L)).thenReturn(account);

        // Create fake transactions
        Transaction deposit = new Transaction();
        deposit.setType(TransactionType.DEPOSIT);
        deposit.setAmount(new BigDecimal("500.00"));
        deposit.setCreatedAt(Instant.parse("2026-11-20T10:00:00Z"));
        deposit.setAccount(account);

        Transaction withdrawal = new Transaction();
        withdrawal.setType(TransactionType.WITHDRAWAL);
        withdrawal.setAmount(new BigDecimal("100.00"));
        withdrawal.setCreatedAt(Instant.parse("2026-11-21T10:00:00Z"));
        withdrawal.setAccount(account);


        List<Transaction> transactions = List.of(deposit, withdrawal);

        // Tell Mockito: when sumNetBefore is called, return 1000.
        when(transactionRepository.sumNetBefore(eq(1L), any(Instant.class))).thenReturn(
                new BigDecimal("1000.00"));

        // Tell Mockito: when findByAccountId... is called, return our transactions
        when(transactionRepository.findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAscIdAsc(
                eq(1L), any(Instant.class), any(Instant.class))).thenReturn(
                List.of(deposit, withdrawal));

        // === ACT (Call the method) ===
        StatementResponse result = statementService.getStatement(1L, 1L, LocalDate.of(2026, 11, 20),

                                                                 LocalDate.of(2026, 11, 22));


        // === ASSERT (Check results) ===
        assertThat(result).isNotNull();
        assertThat(result.openingBalance()).isEqualByComparingTo("1000.00");
        assertThat(result.totalCredits()).isEqualByComparingTo("500.00");
        assertThat(result.totalDebits()).isEqualByComparingTo("100.00");
        assertThat(result.closingBalance()).isEqualByComparingTo("1400.00");
        List<TransactionResponse> entries = result.entries();
        assertThat(entries).hasSize(2);
    }

    @Test
    void getStatement_whenOnlyCredits_returnsCorrectTotals() {
        // === ARRANGE ===
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("4000123456789012");
        account.setCurrency("USD");
        when(accountService.getOwned(1L, 1L)).thenReturn(account);

        // Two deposits, no withdrawals
        Transaction withdrawal1 = new Transaction();
        withdrawal1.setType(TransactionType.DEPOSIT);
        withdrawal1.setAmount(new BigDecimal("300.00"));
        withdrawal1.setCreatedAt(Instant.parse("2026-11-20T10:00:00Z"));
        withdrawal1.setAccount(account);

        Transaction withdrawal2 = new Transaction();
        withdrawal2.setType(TransactionType.DEPOSIT);
        withdrawal2.setAmount(new BigDecimal("200.00"));
        withdrawal2.setCreatedAt(Instant.parse("2026-11-21T10:00:00Z"));
        withdrawal2.setAccount(account);

        // Opening Balance
        when(transactionRepository.sumNetBefore(eq(1L), any(Instant.class))).thenReturn(
                new BigDecimal("500.00"));

        when(transactionRepository.findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAscIdAsc(
                eq(1L), any(Instant.class), any(Instant.class))).thenReturn(
                List.of(withdrawal1, withdrawal2));

        // === ACT ===
        StatementResponse result = statementService.getStatement(1L, 1L, LocalDate.of(2026, 11, 20),
                                                                 LocalDate.of(2026, 11, 22));

        // === ASSERT ===
        assertThat(result).isNotNull();
        assertThat(result.openingBalance()).isEqualByComparingTo("500.00");
        assertThat(result.totalDebits()).isEqualByComparingTo("0.00");
        assertThat(result.totalCredits()).isEqualByComparingTo("500.00");
        assertThat(result.closingBalance()).isEqualByComparingTo("1000.00");
        List<TransactionResponse> entries = result.entries();
        assertThat(entries).hasSize(2);
        assertThat(entries
                           .get(0)
                           .amount()).isEqualByComparingTo("300.00");
        assertThat(entries
                           .get(1)
                           .amount()).isEqualByComparingTo("200.00");


    }

    @Test
    void getStatement_whenOnlyDebits_returnsCorrectTotals() {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("4000123456789012");
        account.setCurrency("USD");
        when(accountService.getOwned(1L, 1L)).thenReturn(account);

        Transaction withdrawal1 = new Transaction();
        withdrawal1.setType(TransactionType.WITHDRAWAL);
        withdrawal1.setAmount(new BigDecimal("300.00"));
        withdrawal1.setCreatedAt(Instant.parse("2026-11-20T10:00:00Z"));
        withdrawal1.setAccount(account);

        Transaction withdrawal2 = new Transaction();
        withdrawal2.setType(TransactionType.WITHDRAWAL);
        withdrawal2.setAmount(new BigDecimal("200.00"));
        withdrawal2.setCreatedAt(Instant.parse("2026-11-21T10:00:00Z"));
        withdrawal2.setAccount(account);

        when(transactionRepository.sumNetBefore(eq(1L), any(Instant.class))).thenReturn(
                new BigDecimal("600.00"));

        when(transactionRepository.findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAscIdAsc(
                eq(1L), any(Instant.class), any(Instant.class))).thenReturn(
                List.of(withdrawal1, withdrawal2));


        StatementResponse result = statementService.getStatement(1L, 1L, LocalDate.of(2026, 11, 20),
                                                                 LocalDate.of(2026, 11, 22));

        List<TransactionResponse> entries = result.entries();

        assertThat(result).isNotNull();
        assertThat(result.openingBalance()).isEqualByComparingTo("600.00");
        assertThat(result.totalDebits()).isEqualByComparingTo("500.00");
        assertThat(result.totalCredits()).isEqualByComparingTo("0.00");
        assertThat(result.closingBalance()).isEqualByComparingTo("100.00");
        assertThat(entries).hasSize(2);
        assertThat(entries
                           .get(0)
                           .amount()).isEqualByComparingTo("300.00");
        assertThat(entries
                           .get(1)
                           .amount()).isEqualByComparingTo("200.00");
    }

    @Test
    void getStatement_whenNoTransactions_returnsZeros() {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("4000123456789012");
        account.setCurrency("USD");
        when(accountService.getOwned(1L, 1L)).thenReturn(account);

        when(transactionRepository.sumNetBefore(eq(1L), any(Instant.class))).thenReturn(
                new BigDecimal("0.00"));

        when(transactionRepository.findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAscIdAsc(
                eq(1L), any(Instant.class), any(Instant.class))).thenReturn(
                List.of());

        StatementResponse result = statementService.getStatement(1L, 1L, LocalDate.of(2026, 11, 22),
                                                                 LocalDate.of(2026, 11, 22));

        assertThat(result).isNotNull();
        assertThat(result.openingBalance()).isEqualByComparingTo("0.00");
        assertThat(result.totalDebits()).isEqualByComparingTo("0.00");
        assertThat(result.totalCredits()).isEqualByComparingTo("0.00");
        assertThat(result.closingBalance()).isEqualByComparingTo("0.00");
        assertThat(result.entries()).isEmpty();
    }


}