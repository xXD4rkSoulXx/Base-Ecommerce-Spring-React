package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    boolean hasUserPurchasedProduct(
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            @Param("status") OrderStatus status
    );
}
