package com.nabin.bankingwallet.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    /**
     * Sum of outgoing money since a point in time (Step 8). Returns null when there are no rows.
     */
    @Query("select sum(t.amount) from Transaction t where t.account.id = :accountId "
           + "and t.type in :types and t.createdAt >= :since")
    BigDecimal sumOutgoingSince(
            @Param("accountId")
            Long accountId,
            @Param("types")
            Collection<TransactionType> types,
            @Param("since")
            Instant since);

}
