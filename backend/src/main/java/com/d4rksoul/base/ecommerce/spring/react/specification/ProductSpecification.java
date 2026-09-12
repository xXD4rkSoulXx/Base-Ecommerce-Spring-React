package com.d4rksoul.base.ecommerce.spring.react.specification;

import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;
import jakarta.persistence.criteria.Expression;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductSpecification {

    public static Specification<Product> hasCategory(ProductCategory category) {
        return (root, _, cb) -> {
            if (category == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("category"), category);
        };
    }

    public static Specification<Product> containsNameOrRef(String q) {
        return (root, _, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }

            return cb.or(
                    cb.like(cb.lower(root.get("name")), "%" + q.trim().toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("ref")), "%" + q.trim().toLowerCase() + "%")
            );
        };
    }

    public static Specification<Product> orderByEffectivePrice(String sortBy) {
        return (root, query, cb) -> {
            Expression<Number> discountExpression = cb.quot(root.get("discountPercentage"), 100.0);
            Expression<Number> multiplierExpression = cb.diff(1.0, discountExpression);
            Expression<Number> effectivePrice = cb.prod(root.get("price"), multiplierExpression);

            if ("price_asc".equals(sortBy)) {
                query.orderBy(cb.asc(effectivePrice), cb.asc(root.get("id")));
            } else {
                query.orderBy(cb.desc(effectivePrice), cb.asc(root.get("id")));
            }

            return cb.conjunction();
        };
    }

    public static Specification<Product> hasDiscount() {
        return (root, _, cb) ->
                cb.greaterThan(root.get("discountPercentage"), 0);
    }

    public static Specification<Product> orderByDiscountDesc() {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("discountPercentage")), cb.asc(root.get("id")));

            return cb.conjunction();
        };
    }

    public static Specification<Product> orderByRecentDesc() {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("createdAt")), cb.asc(root.get("id")));

            return cb.conjunction();
        };
    }
}
