package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.CategoryUnitsProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.TopProductProjection;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
        SELECT new com.d4rksoul.base.ecommerce.spring.react.dto.projection.TopProductProjection(
            p.id,
            p.ref,
            p.name,
            SUM(i.quantity)
        )
        FROM OrderItem i
        JOIN i.order o
        JOIN i.productSku sku
        JOIN sku.product p
        WHERE o.status IN :statuses
        AND o.paidAt >= :start
        AND o.paidAt < :end
        GROUP BY p.id, p.ref, p.name
        ORDER BY SUM(i.quantity) DESC, p.id ASC
    """)
    List<TopProductProjection> findTopProducts(
        @Param("statuses") Collection<OrderStatus> statuses,
        @Param("start") Instant start,
        @Param("end") Instant end,
        Pageable pageable
    );

    boolean existsByImagePath(String imagePath);

    @Query("""
        SELECT new com.d4rksoul.base.ecommerce.spring.react.dto.projection.CategoryUnitsProjection(
            p.category,
            SUM(i.quantity)
        )
        FROM OrderItem i
        JOIN i.order o
        JOIN i.productSku sku
        JOIN sku.product p
        WHERE o.status IN :statuses
        AND o.paidAt >= :start
        AND o.paidAt < :end
        GROUP BY p.category
        ORDER BY SUM(i.quantity) DESC
    """)
    List<CategoryUnitsProjection> sumUnitsByCategory(
        @Param("statuses") Collection<OrderStatus> statuses,
        @Param("start") Instant start,
        @Param("end") Instant end
    );
}
