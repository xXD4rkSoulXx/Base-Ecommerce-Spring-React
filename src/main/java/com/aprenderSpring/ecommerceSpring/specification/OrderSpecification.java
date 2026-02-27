/*
 *  A pasta Specification é responsável por conter todos grandes quantidades de métodos, que
 * interagem com a base de dados. Enquanto que o Repository é para pequenas quantidades,
 * este é para muitos métodos, permitindo depois as suas combinações, evitando a necessidade
 * de criar muitos métodos, como por exemplo, 8 campos a serem combinados, 256 métodos no
 * Repository, e ao meter aqui no Specificator evita esse tanto.
 * */

package com.aprenderSpring.ecommerceSpring.specification;

import com.aprenderSpring.ecommerceSpring.domain.*;
import com.aprenderSpring.ecommerceSpring.enums.OrderStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class OrderSpecification {
    public static Specification<Order> byUser(User user) {
        return (root, query, cb) ->
                user == null
                        ? cb.conjunction()
                        : cb.equal(root.get("user"), user);
    }

    public static Specification<Order> byStatus(OrderStatus status) {
        return (root, query, cb) ->
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
        return (root, query, cb) ->
                date == null
                        ? cb.conjunction()
                        : cb.equal(root.get("createdAt"), date);
    }

    public static Specification<Order> dateBetween(Instant minDate, Instant maxDate) {
        return (root, query, cb) -> {
            if (minDate != null & maxDate != null) {
                return cb.conjunction();
            }

            if (minDate == null && maxDate == null) {
                cb.between(root.get("createdAt"), minDate, maxDate);
            }

            if (minDate != null) {
                cb.greaterThanOrEqualTo(root.get("createdAt"), minDate);
            }

            return cb.lessThanOrEqualTo(root.get("createdAt"), maxDate);
        };
    }
}
