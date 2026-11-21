package com.nabin.bankingwallet.transaction;

import com.nabin.bankingwallet.transaction.dto.response.StatementResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatementService {

    public static final long
            MAX_RANGE_DAYS = 366;


    @Transactional(readOnly = true)
    public StatementResponse getStatement(Long userId, Long accountId, LocalDate from,
                                          LocalDate to) {


        return null;

    }
}
