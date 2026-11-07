package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.CustomerOrderStatsProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.DiscountCodeUsageProjection;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    long countByDiscountCodeId(Long discountCodeId);

    @Query("""
        SELECT new com.d4rksoul.base.ecommerce.spring.react.dto.projection.CustomerOrderStatsProjection(
            o.user.id,
            COUNT(o),
            MAX(o.createdAt)
        )
        FROM Order o
        WHERE o.user.id IN :userIds
        AND o.status IN :statuses
        GROUP BY o.user.id
    """)
    List<CustomerOrderStatsProjection> findCustomerStatsByUserIds(
            @Param("userIds") Collection<Long> userIds,
            @Param("statuses") Collection<OrderStatus> statuses
    );

    @Query("""
        SELECT new com.d4rksoul.base.ecommerce.spring.react.dto.projection.DiscountCodeUsageProjection(
            o.discountCode.id,
            COUNT(o)
        )
        FROM Order o
        WHERE o.discountCode.id IN :discountCodeIds
        GROUP BY o.discountCode.id
    """)
    List<DiscountCodeUsageProjection> countOrdersByDiscountCodeIds(
            @Param("discountCodeIds") Collection<Long> discountCodeIds
    );

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    Optional<Order> findByIdAndStatusIn(Long id, Collection<OrderStatus> statuses);

    long countByUserIdAndStatusIn(Long userId, Collection<OrderStatus> statuses);

    Optional<Order> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);

    List<Order> findByStatusAndStripeCheckoutSessionIdNotNullAndCreatedAtBefore(OrderStatus status, Instant limit);

    List<Order> findByStatusAndPaidAtBeforeAndManuallyManagedFalse(OrderStatus status, Instant limit);

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
        AND o.manuallyManaged = false
        AND NOT EXISTS (SELECT 1 FROM OrderShipment s WHERE s.order = o)
    """)
    List<Order> findByStatusAndPaidAtBeforeWithoutShipments(
            @Param("status") OrderStatus status,
            @Param("limit") Instant limit
    );

    @Query("""
        SELECT o FROM Order o
        WHERE o.status = :status
        AND o.manuallyManaged = false
        AND EXISTS (SELECT 1 FROM OrderShipment s WHERE s.order = o AND s.status <> :excluded)
    """)
    List<Order> findWithAnyShipmentOtherThan(
            @Param("status") OrderStatus status,
            @Param("excluded") ShipmentStatus excluded
    );

    @Query("""
        SELECT o FROM Order o
        WHERE o.status = :status
        AND o.manuallyManaged = false
        AND EXISTS (SELECT 1 FROM OrderShipment s WHERE s.order = o)
        AND NOT EXISTS (SELECT 1 FROM OrderShipment s WHERE s.order = o AND s.status <> :shipmentStatus)
    """)
    List<Order> findWithAllShipmentsIn(
            @Param("status") OrderStatus status,
            @Param("shipmentStatus") ShipmentStatus shipmentStatus
    );

    @Override
    @EntityGraph(attributePaths = "user")
    @NonNull
    Page<Order> findAll(@NonNull Specification<Order> specification, @NonNull Pageable pageable);

    @Modifying
    @Transactional
    @Query("update Order o set o.manuallyManaged = true where o.id = :id")
    void markManuallyManaged(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("update Order o set o.status = :to where o.id = :id and o.status = :from")
    int updateStatus(@Param("id") Long id, @Param("from") OrderStatus from, @Param("to") OrderStatus to);
}
