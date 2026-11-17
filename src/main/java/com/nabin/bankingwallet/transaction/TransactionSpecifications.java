package com.nabin.bankingwallet.transaction;

import com.nabin.bankingwallet.transaction.dto.TransactionFilter;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public final class TransactionSpecifications {
    private TransactionSpecifications() {
    }

    public static Specification<Transaction> forAccount(Long accountId) {
        return accountId == null ? null :
               (root, query, criteriaBuilder) -> criteriaBuilder.equal(root
                                                                       .get("account")
                                                                       .get("id"),
                                                                       accountId);
    }

    public static Specification<Transaction> forFilter(TransactionFilter filter) {
        if (filter == null) {
            return null;
        }
        return Specification
                .where(hasType(filter.transactionType()))
                .and(minAmount(filter.minAmount()))
                .and(maxAmount(filter.maxAmount()))
                .and(createdFrom(filter.fromDate()))
                .and(createdBefore(filter.toDate()));
    }

    private static Specification<Transaction> hasType(TransactionType type) {
        return type == null ? null : (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    private static Specification<Transaction> minAmount(BigDecimal min) {
        return min == null ? null
                           :
               (root, query, cb) -> cb.greaterThanOrEqualTo(root.<BigDecimal>get("amount"), min);
    }

    private static Specification<Transaction> maxAmount(BigDecimal max) {
        return max == null ? null
                           :
               (root, query, cb) -> cb.lessThanOrEqualTo(root.<BigDecimal>get("amount"), max);
    }

    private static Specification<Transaction> createdFrom(LocalDate from) {
        if (from == null) {
            return null;
        }
        Instant start = from
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.<Instant>get("createdAt"), start);
    }

    /**
     * 'to' is inclusive: compare against the start of the NEXT day.
     */
    private static Specification<Transaction> createdBefore(LocalDate to) {
        if (to == null) {
            return null;
        }
        Instant end = to
                .plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        return (root, query, cb) -> cb.lessThan(root.<Instant>get("createdAt"), end);
    }

}
