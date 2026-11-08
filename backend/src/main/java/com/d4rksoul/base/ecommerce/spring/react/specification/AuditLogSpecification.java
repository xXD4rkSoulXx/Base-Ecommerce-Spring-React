package com.d4rksoul.base.ecommerce.spring.react.specification;

import com.d4rksoul.base.ecommerce.spring.react.entity.AuditLog;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditEntityType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditLogSpecification {

    public static Specification<AuditLog> containsQuery(String q) {
        return (root, _, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + q.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("actorEmail")), pattern),
                    cb.like(cb.lower(root.get("entityLabel")), pattern)
            );
        };
    }

    public static Specification<AuditLog> hasEntityType(AuditEntityType entityType) {
        return (root, _, cb) -> {
            if (entityType == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("entityType"), entityType);
        };
    }

    public static Specification<AuditLog> hasAction(AuditAction action) {
        return (root, _, cb) -> {
            if (action == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("action"), action);
        };
    }

    public static Specification<AuditLog> orderByRecentDesc() {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("createdAt")), cb.desc(root.get("id")));

            return cb.conjunction();
        };
    }
}
