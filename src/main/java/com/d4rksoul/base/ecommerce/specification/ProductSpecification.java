package com.d4rksoul.base.ecommerce.specification;

import com.d4rksoul.base.ecommerce.domain.Category;
import com.d4rksoul.base.ecommerce.domain.Discount;
import com.d4rksoul.base.ecommerce.domain.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@UtilityClass
public class ProductSpecification {
    private final String PRICE = "price";

    public static Specification<Product> nameContains(String name) {
        return (root, _, cb) ->
                name == null
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Product> descriptionContains(String description) {
        return (root, _, cb) ->
                description == null
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    }

    public static Specification<Product> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, _, cb) -> {
            if (minPrice == null && maxPrice == null) {
                return cb.conjunction();
            }

            if (minPrice != null && maxPrice != null) {
                return cb.between(root.get(PRICE), minPrice, maxPrice);
            }

            if (minPrice != null) {
                return cb.greaterThanOrEqualTo(root.get(PRICE), minPrice);
            }

            return cb.lessThanOrEqualTo(root.get(PRICE), maxPrice);
        };
    }

    public static Specification<Product> byCategory(Category category) {
        return (root, _, cb) ->
                category == null
                        ? cb.conjunction()
                        : cb.equal(root.get("category"), category);
    }

    public static Specification<Product> byCategories(List<Category> categories) {
        return (root, _, cb) ->
                categories == null || categories.isEmpty()
                        ? cb.conjunction()
                        : root.get("category").in(categories);
    }

    public static Specification<Product> hasActiveDiscount(Instant now) {
        return (root, _, cb) -> {
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
        return (root, _, cb) -> {
            if (targetDate == null) {
                return cb.conjunction();
            }

            Join<Product, Discount> discount = root.join("discount", JoinType.INNER);

            return cb.lessThanOrEqualTo(discount.get("endDate"), targetDate);
        };
    }
}
