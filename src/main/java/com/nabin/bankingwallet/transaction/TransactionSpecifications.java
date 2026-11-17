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

    /**
     * Always-true predicate used as a no-op so {@code Specification.where(...).and(...)}
     * never receives null. Returning null breaks the chain with
     * "Specification must not be null".
     */
    private static final Specification<Transaction> ALWAYS_TRUE =
            (root, query, cb) -> cb.conjunction();

    public static Specification<Transaction> forAccount(Long accountId) {
        if (accountId == null) {
            return ALWAYS_TRUE;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root
                .get("account")
                .get("id"),
                accountId);
    }

    public static Specification<Transaction> forFilter(TransactionFilter filter) {
        if (filter == null) {
            return ALWAYS_TRUE;
        }
        return Specification
                .where(hasType(filter.type()))
                .and(minAmount(filter.minAmount()))
                .and(maxAmount(filter.maxAmount()))
                .and(createdFrom(filter.fromDate()))
                .and(createdBefore(filter.toDate()));
    }

    private static Specification<Transaction> hasType(TransactionType type) {
        return type == null ? ALWAYS_TRUE
                            : (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    private static Specification<Transaction> minAmount(BigDecimal min) {
        return min == null ? ALWAYS_TRUE
                           :
               (root, query, cb) -> cb.greaterThanOrEqualTo(root.<BigDecimal>get("amount"), min);
    }

    private static Specification<Transaction> maxAmount(BigDecimal max) {
        return max == null ? ALWAYS_TRUE
                           :
               (root, query, cb) -> cb.lessThanOrEqualTo(root.<BigDecimal>get("amount"), max);
    }

    private static Specification<Transaction> createdFrom(LocalDate from) {
        if (from == null) {
            return ALWAYS_TRUE;
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
            return ALWAYS_TRUE;
        }
        Instant end = to
                .plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        return (root, query, cb) -> cb.lessThan(root.<Instant>get("createdAt"), end);
    }

}
