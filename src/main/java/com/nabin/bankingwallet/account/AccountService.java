package com.nabin.bankingwallet.account;

import com.nabin.bankingwallet.account.dto.request.CreateAccountRequest;
import com.nabin.bankingwallet.account.dto.response.AccountResponse;
import com.nabin.bankingwallet.common.exception.ResourceNotFoundException;
import com.nabin.bankingwallet.common.util.AccountNumberGenerator;
import com.nabin.bankingwallet.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    @Transactional
    public AccountResponse createAccount(Long userId, CreateAccountRequest createAccountRequest) {
        Account account = new Account();
        account.setCurrency(createAccountRequest.currency());
        account.setUser(userRepository.getReferenceById(userId));

        String number;
        do {
            number = accountNumberGenerator.generateAccountNumber();
        } while (accountRepository.existsByAccountNumber((number)));

        account.setAccountNumber(number);

        accountRepository.save(account);
        return AccountResponse.fromAccount(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listMine(Long userId) {
        List<Account> accounts = accountRepository.findAllByUser_IdOrderByIdAsc(userId);

        return accounts
                .stream()
                .map(AccountResponse::fromAccount)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse get(Long accountId, Long userId) {
        return AccountResponse.fromAccount(getOwned(accountId, userId));
    }

    /**
     * Plain read. Returns 404 (not 403) when the account belongs to someone else.
     */
    @Transactional(readOnly = true)
    public Account getOwned(Long accountId, Long userId) {
        return accountRepository
                .findByIdAndUser_Id(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    /**
     * Locks the row (SELECT ... FOR UPDATE). Call it only inside a @Transactional method.
     */
    public Account getOwnedForUpdate(Long accountId, Long userId) {
        return accountRepository
                .findByIdAndUser_IdForUpdate(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }


}
