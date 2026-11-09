package com.d4rksoul.base.ecommerce.spring.react.specification;

import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductSkuSpecification {

    public static Specification<ProductSku> isActive() {
        return (root, _, cb) -> cb.and(
                cb.isTrue(root.get("active")),
                cb.isTrue(root.get("product").get("active"))
        );
    }

    public static Specification<ProductSku> containsProductNameOrRef(String q) {
        return (root, _, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + q.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("product").get("name")), pattern),
                    cb.like(cb.lower(root.get("product").get("ref")), pattern)
            );
        };
    }

    public static Specification<ProductSku> hasLowStock(boolean lowStock, int threshold) {
        return (root, _, cb) -> {
            if (!lowStock) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(root.get("stock"), threshold);
        };
    }

    public static Specification<ProductSku> orderByProductName() {
        return (root, query, cb) -> {
            query.orderBy(
                    cb.asc(root.get("product").get("name")),
                    cb.asc(root.get("product").get("id")),
                    cb.asc(root.get("id"))
            );

            return cb.conjunction();
        };
    }

    public static Specification<ProductSku> orderByStockAsc() {
        return (root, query, cb) -> {
            query.orderBy(cb.asc(root.get("stock")), cb.asc(root.get("id")));

            return cb.conjunction();
        };
    }
}
