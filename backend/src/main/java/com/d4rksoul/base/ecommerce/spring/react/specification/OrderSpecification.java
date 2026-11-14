package com.d4rksoul.base.ecommerce.spring.react.specification;

import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Collection;
import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderSpecification {

    public static Specification<Order> matchesSearch(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }

            Subquery<Long> items = query.subquery(Long.class);
            Root<OrderItem> item = items.from(OrderItem.class);

            items.select(item.get("id")).where(
                cb.equal(item.get("order"), root),
                cb.or(
                    cb.like(cb.lower(
                        item.get("productName")),
                        "%" + q.trim().toLowerCase(Locale.ROOT) + "%"
                    ),
                    cb.like(cb.lower(
                        item.get("productSku").get("product").get("ref")),
                        "%" + q.trim().toLowerCase(Locale.ROOT) + "%"
                    )
                )
            );

            return orMatchesId(cb, root, cb.exists(items), q.trim().toLowerCase(Locale.ROOT));
        };
    }

    public static Specification<Order> matchesCustomerSearch(String q) {
        return (root, _, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }

            Predicate matchesText = cb.or(
                cb.like(cb.lower(
                    root.get("user").get("email")),
                    "%" + q.trim().toLowerCase(Locale.ROOT) + "%"
                ),
                cb.like(cb.lower(
                    root.get("recipientName")),
                    "%" + q.trim().toLowerCase(Locale.ROOT) + "%"
                )
            );

            return orMatchesId(cb, root, matchesText, q.trim().toLowerCase(Locale.ROOT));
        };
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, _, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Order> hasStatusIn(Collection<OrderStatus> statuses) {
        return (root, _, _) -> root.get("status").in(statuses);
    }

    public static Specification<Order> belongsToUser(Long userId) {
        return (root, _, cb) ->
            cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Order> createdFrom(Instant from) {
        return (root, _, cb) -> {
            if (from == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
        };
    }

    public static Specification<Order> createdTo(Instant to) {
        return (root, _, cb) -> {
            if (to == null) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    public static Specification<Order> orderByRecentDesc() {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("createdAt")), cb.asc(root.get("id")));

            return cb.conjunction();
        };
    }

    public static Specification<Order> orderByFulfillmentPriority() {
        return (root, query, cb) -> {
            CriteriaBuilder.SimpleCase<OrderStatus, Integer> priority = cb.selectCase(root.get("status"));

            for (int i = 0; i < OrderStatus.FULFILLMENT_STATUSES.size(); i++) {
                priority.when(OrderStatus.FULFILLMENT_STATUSES.get(i), i);
            }

            query.orderBy(
                cb.asc(priority.otherwise(OrderStatus.FULFILLMENT_STATUSES.size())),
                cb.asc(root.get("createdAt")),
                cb.asc(root.get("id"))
            );

            return cb.conjunction();
        };
    }

    private static Predicate orMatchesId(
        CriteriaBuilder cb, Root<Order> root, Predicate textMatch, String term
    ) {
        String number = term.startsWith("#") ? term.substring(1) : term;

        if (!number.matches("\\d{1,18}")) {
            return textMatch;
        }

        return cb.or(textMatch, cb.equal(root.get("id"), Long.parseLong(number)));
    }
}
