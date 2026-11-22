package com.nabin.bankingwallet.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

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

    /**
     * Eagerly fetch the owning account so the history endpoint does not
     * trigger a lazy-load per row (N+1). {@code account} is otherwise LAZY
     * and {@code TransactionResponse.fromTransaction} reads its id.
     */
    @EntityGraph(attributePaths = {"account"})
    Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);

    @Query("""
            select coalesce(sum(case when t.type in ('DEPOSIT', 'TRANSFER_IN') then t.amount when t.type in ('WITHDRAWAL', 'TRANSFER_OUT') then -t.amount else 0 end) , 0)
                        from Transaction t where t.account.id = :accountId and t.createdAt < :fromInstant
            """)
    BigDecimal sumNetBefore(
            @Param("accountId")
            Long accountId,
            @Param("fromInstant")
            Instant fromInstant);

    @EntityGraph(attributePaths = {"account"})
    List<Transaction> findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAscIdAsc(
            @Param("accountId")
            Long accountId,
            @Param("fromInstant")
            Instant fromInstant,
            @Param("toInstant")
            Instant toInstant);
}
