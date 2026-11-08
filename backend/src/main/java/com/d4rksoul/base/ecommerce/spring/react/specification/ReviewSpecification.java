package com.d4rksoul.base.ecommerce.spring.react.specification;

import com.d4rksoul.base.ecommerce.spring.react.entity.Review;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewSpecification {

    public static Specification<Review> containsQuery(String q) {
        return (root, _, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + q.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("product").get("name")), pattern),
                    cb.like(cb.lower(root.get("product").get("ref")), pattern),
                    cb.like(cb.lower(root.get("user").get("email")), pattern),
                    cb.like(
                            cb.lower(cb.concat(
                                    cb.concat(root.get("user").<String>get("firstName"), " "),
                                    root.get("user").<String>get("lastName")
                            )),
                            pattern
                    ),
                    cb.like(cb.lower(root.get("title")), pattern)
            );
        };
    }

    public static Specification<Review> hasHidden(Boolean hidden) {
        return (root, _, cb) -> {
            if (hidden == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("hidden"), hidden);
        };
    }

    public static Specification<Review> orderByRecentDesc() {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("createdAt")), cb.asc(root.get("id")));

            return cb.conjunction();
        };
    }
}
