package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

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

    List<Order> findByStatusAndPaidAtBefore(OrderStatus status, Instant limit);

    @EntityGraph(attributePaths = {
            "discountCode",
            "orderItems",
            "orderItems.productSku",
            "orderItems.productSku.product"
    })
    Optional<Order> findByIdAndUserIdAndStatusIn(Long id, Long userId, Collection<OrderStatus> statuses);

    @Query("""
        SELECT o FROM Order o
        WHERE o.status = :status
        AND o.paidAt < :limit
        AND NOT EXISTS (SELECT 1 FROM OrderShipment s WHERE s.order = o)
    """)
    List<Order> findByStatusAndPaidAtBeforeWithoutShipments(
            @Param("status") OrderStatus status,
            @Param("limit") Instant limit
    );

    @Query("""
        SELECT o FROM Order o
        WHERE o.status = :status
        AND EXISTS (SELECT 1 FROM OrderShipment s WHERE s.order = o AND s.status <> :excluded)
    """)
    List<Order> findWithAnyShipmentOtherThan(
            @Param("status") OrderStatus status,
            @Param("excluded") ShipmentStatus excluded
    );

    @Query("""
        SELECT o FROM Order o
        WHERE o.status = :status
        AND EXISTS (SELECT 1 FROM OrderShipment s WHERE s.order = o)
        AND NOT EXISTS (SELECT 1 FROM OrderShipment s WHERE s.order = o AND s.status <> :shipmentStatus)
    """)
    List<Order> findWithAllShipmentsIn(
            @Param("status") OrderStatus status,
            @Param("shipmentStatus") ShipmentStatus shipmentStatus
    );

    @Modifying
    @Transactional
    @Query("update Order o set o.status = :to where o.id = :id and o.status = :from")
    int updateStatus(@Param("id") Long id, @Param("from") OrderStatus from, @Param("to") OrderStatus to);
}
