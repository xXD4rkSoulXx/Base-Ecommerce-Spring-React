/*
 *  A pasta Specification é responsável por conter todos grandes quantidades de métodos, que
 * interagem com a base de dados. Enquanto que o Repository é para pequenas quantidades,
 * este é para muitos métodos, permitindo depois as suas combinações, evitando a necessidade
 * de criar muitos métodos, como por exemplo, 8 campos a serem combinados, 256 métodos no
 * Repository, e ao meter aqui no Specificator evita esse tanto.
 * */

package com.aprenderSpring.ecommerceSpring.specification;

import com.aprenderSpring.ecommerceSpring.domain.Category;
import com.aprenderSpring.ecommerceSpring.domain.Discount;
import com.aprenderSpring.ecommerceSpring.domain.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class ProductSpecification {
    public static Specification<Product> nameContains(String name) {
        return (root, query, cb) ->
                name == null
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Product> descriptionContains(String description) {
        return (root, query, cb) ->
                description == null
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    }

    public static Specification<Product> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) {
                return cb.conjunction();
            }

            if (minPrice != null && maxPrice != null) {
                return cb.between(root.get("price"), minPrice, maxPrice);
            }

            if (minPrice != null) {
                return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            }

            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    public static Specification<Product> byCategory(Category category) {
        return (root, query, cb) ->
                category == null
                        ? cb.conjunction()
                        : cb.equal(root.get("category"), category);
    }

    public static Specification<Product> byCategories(List<Category> categories) {
        return (root, query, cb) ->
                categories == null || categories.isEmpty()
                        ? cb.conjunction()
                        : root.get("category").in(categories);
    }

    public static Specification<Product> hasActiveDiscount(Instant now) {
        return (root, query, cb) -> {
            if (now == null) {
                return cb.conjunction();
            }

            Join<Product, Discount> discount = root.join("discount", JoinType.INNER);

            return cb.and(
                    cb.lessThanOrEqualTo(discount.get("startDate"), now),
                    cb.greaterThanOrEqualTo(discount.get("endDate"), now)
            );
        };
    }

    public static Specification<Product> discountEndsBefore(Instant targetDate) {
        return (root, query, cb) -> {
            if (targetDate == null) {
                return cb.conjunction();
            }

            Join<Product, Discount> discount = root.join("discount", JoinType.INNER);

            return cb.lessThanOrEqualTo(discount.get("endDate"), targetDate);
        };
    }
}
