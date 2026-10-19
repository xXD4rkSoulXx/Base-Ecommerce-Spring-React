package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        SELECT COUNT(o) > 0 
        FROM Order o 
        JOIN o.orderItems item 
        JOIN item.productSku sku
        WHERE o.user.id = :userId 
        AND sku.product.id = :productId 
        AND o.status = :status
    """)
    boolean existsUserPurchasedProduct(
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            @Param("status") OrderStatus status
    );

    boolean existsByUserIdAndDiscountCodeIdAndStatusNotIn(
            Long userId, Long discountCodeId, Collection<OrderStatus> statuses
    );

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    Optional<Order> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);

    List<Order> findByStatusAndStripeCheckoutSessionIdNotNullAndCreatedAtBefore(OrderStatus status, Instant limit);

    @Modifying
    @Query("update Order o set o.status = :to where o.id = :id and o.status = :from")
    int updateStatus(@Param("id") Long id, @Param("from") OrderStatus from, @Param("to") OrderStatus to);
}
