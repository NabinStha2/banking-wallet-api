package com.nabin.bankingwallet.common.util;

import com.nabin.bankingwallet.common.exception.BusinessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.Set;

public final class PageUtils {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "amount", "id");

    private PageUtils() {
    }

    public static Pageable buildPageable(Pageable pageable) {
        Sort sort = pageable
                            .getSort()
                            .isUnsorted()
                    ? Sort.by(Sort.Order.desc("createdAt"))
                    : pageable.getSort();
        for (Sort.Order order : sort) {
            if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_SORT",
                                            "Cannot sort by: " + order.getProperty());
            }
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                              sort.and(Sort.by(Sort.Order.desc("id"))));
    }
}

//eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ3aG9jYXJlc0Bnb29nbGUuY29tIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3OTQ5Mjc3MjgsImV4cCI6MTc5NDkzMTMyOH0.3gHcQ9hg0V65x3OjEgHGBbla-gSSjuc24avuSsLNOAU