package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderReturnRepository extends JpaRepository<OrderReturn, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OrderReturn> findByIdAndStatus(Long id, ReturnStatus status);

    List<OrderReturn> findByStatusAndCreatedAtBefore(ReturnStatus status, Instant createdAt);

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
}
