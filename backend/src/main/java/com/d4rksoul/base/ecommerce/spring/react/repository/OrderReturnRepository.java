package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderReturnRepository
        extends JpaRepository<OrderReturn, Long>, JpaSpecificationExecutor<OrderReturn> {

    @Override
    @EntityGraph(attributePaths = {"order", "order.user"})
    @NonNull
    Page<OrderReturn> findAll(@NonNull Specification<OrderReturn> specification, @NonNull Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OrderReturn> findByIdAndStatus(Long id, ReturnStatus status);

    List<OrderReturn> findByStatusAndCreatedAtBeforeAndManuallyManagedFalse(
            ReturnStatus status, Instant createdAt
    );

    List<OrderReturn> findByStatusAndApprovedAtBeforeAndManuallyManagedFalse(
            ReturnStatus status, Instant approvedAt
    );

    List<OrderReturn> findByStatusAndReceivedAtBeforeAndManuallyManagedFalse(
            ReturnStatus status, Instant receivedAt
    );

    List<OrderReturn> findByOrderIdOrderByCreatedAtAscIdAsc(Long orderId);

    @Query("""
        SELECT COALESCE(SUM(r.refundAmount), 0)
        FROM OrderReturn r
        WHERE r.order.id = :orderId
        AND r.status = :status
    """)
    BigDecimal sumRefundAmountByOrderIdAndStatus(
            @Param("orderId") Long orderId,
            @Param("status") ReturnStatus status
    );

    @Modifying
    @Query("UPDATE OrderReturn r SET r.manuallyManaged = true WHERE r.id = :id")
    void markManuallyManaged(@Param("id") Long id);
}
