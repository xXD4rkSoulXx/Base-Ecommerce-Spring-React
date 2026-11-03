package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ReturnedQuantityProjection;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturnItem;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OrderReturnItemRepository extends JpaRepository<OrderReturnItem, Long> {

    List<OrderReturnItem> findByOrderReturnId(Long orderReturnId);

    @EntityGraph(attributePaths = "orderItem")
    List<OrderReturnItem> findByOrderReturnInOrderByIdAsc(List<OrderReturn> orderReturns);

    @Query("""
        SELECT new com.d4rksoul.base.ecommerce.spring.react.dto.projection.ReturnedQuantityProjection(
            ri.orderItem.id,
            SUM(ri.quantity)
        )
        FROM OrderReturnItem ri
        WHERE ri.orderReturn.order.id = :orderId
        AND ri.orderReturn.status NOT IN :excluded
        GROUP BY ri.orderItem.id
    """)
    List<ReturnedQuantityProjection> sumReturnedQuantities(
            @Param("orderId") Long orderId,
            @Param("excluded") Collection<ReturnStatus> excluded
    );
}
