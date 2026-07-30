package com.d4rksoul.base.ecommerce.spring.react.specification;

import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;
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
}
