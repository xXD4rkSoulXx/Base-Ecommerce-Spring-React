package com.d4rksoul.base.ecommerce.spring.react.specification;

import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserSpecification {

    public static Specification<User> containsNameOrEmail(String q) {
        return (root, _, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }

            return cb.or(
                cb.like(cb.lower(root.get("email")), "%" + q.trim().toLowerCase(Locale.ROOT) + "%"),
                cb.like(
                    cb.lower(cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"))),
                    "%" + q.trim().toLowerCase(Locale.ROOT) + "%"
                )
            );
        };
    }

    public static Specification<User> hasRole(UserRole role) {
        return (root, _, cb) -> cb.equal(root.get("role"), role);
    }

    public static Specification<User> orderByRecentDesc() {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("createdAt")), cb.asc(root.get("id")));

            return cb.conjunction();
        };
    }
}
