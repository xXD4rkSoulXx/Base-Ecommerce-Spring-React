package com.d4rksoul.base.ecommerce.specification;

import com.d4rksoul.base.ecommerce.domain.Order;
import com.d4rksoul.base.ecommerce.domain.OrderItem;
import com.d4rksoul.base.ecommerce.domain.Product;
import com.d4rksoul.base.ecommerce.domain.User;
import com.d4rksoul.base.ecommerce.enums.OrderStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

@UtilityClass
public class OrderSpecification {
    private final String CREATED_AT = "createdAt";

    public static Specification<Order> byUser(User user) {
        return (root, _, cb) ->
                user == null
                        ? cb.conjunction()
                        : cb.equal(root.get("user"), user);
    }

    public static Specification<Order> byStatus(OrderStatus status) {
        return (root, _, cb) ->
                status == null
                        ? cb.conjunction()
                        : cb.equal(root.get("status"), status);
    }

    public static Specification<Order> byProduct(String productName) {
        return (root, query, cb) -> {
            if (productName == null || productName.isBlank()) {
                return cb.conjunction();
            }

            query.distinct(true);

            Join<Order, OrderItem> orderItem = root.join("orderItemList", JoinType.INNER);

            Join<OrderItem, Product> product = orderItem.join("product", JoinType.INNER);

            return cb.like(cb.lower(product.get("name")), "%" + productName.toLowerCase() + "%");
        };
    }

    public static Specification<Order> byDate(Instant date) {
        return (root, _, cb) ->
                date == null
                        ? cb.conjunction()
                        : cb.equal(root.get(CREATED_AT), date);
    }

    public static Specification<Order> dateBetween(Instant minDate, Instant maxDate) {
        return (root, _, cb) -> {
            if (minDate == null && maxDate == null) {
                return cb.conjunction();
            }

            if (minDate != null && maxDate != null) {
                cb.between(root.get(CREATED_AT), minDate, maxDate);
            }

            if (minDate != null) {
                cb.greaterThanOrEqualTo(root.get(CREATED_AT), minDate);
            }

            return cb.lessThanOrEqualTo(root.get(CREATED_AT), maxDate);
        };
    }
}
