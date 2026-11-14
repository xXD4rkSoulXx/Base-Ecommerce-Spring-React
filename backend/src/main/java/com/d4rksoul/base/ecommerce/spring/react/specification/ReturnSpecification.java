package com.d4rksoul.base.ecommerce.spring.react.specification;

import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReturnSpecification {

    public static Specification<OrderReturn> hasStatus(ReturnStatus status) {
        return (root, _, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<OrderReturn> hasStatusIn(Collection<ReturnStatus> statuses) {
        return (root, _, _) -> root.get("status").in(statuses);
    }

    public static Specification<OrderReturn> orderByRecentDesc() {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("createdAt")), cb.asc(root.get("id")));

            return cb.conjunction();
        };
    }

    public static Specification<OrderReturn> orderByPendingPriority() {
        return (root, query, cb) -> {
            CriteriaBuilder.SimpleCase<ReturnStatus, Integer> priority = cb.selectCase(root.get("status"));

            for (int i = 0; i < ReturnStatus.PENDING_STATUSES.size(); i++) {
                priority.when(ReturnStatus.PENDING_STATUSES.get(i), i);
            }

            query.orderBy(
                cb.asc(priority.otherwise(ReturnStatus.PENDING_STATUSES.size())),
                cb.asc(root.get("createdAt")),
                cb.asc(root.get("id"))
            );

            return cb.conjunction();
        };
    }
}
