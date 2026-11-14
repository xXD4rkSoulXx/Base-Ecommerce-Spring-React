package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @EntityGraph(attributePaths = {"productSku", "productSku.product"})
    List<CartItem> findByCartIdOrderByIdAsc(Long cartId);

    Optional<CartItem> findByCartIdAndProductSkuId(Long cartId, Long productSkuId);

    long countByCartId(Long cartId);

    @Modifying
    @Query("DELETE FROM CartItem i WHERE i.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);

    @Modifying
    @Query("DELETE FROM CartItem i WHERE i.cart.id = :cartId AND i.productSku.id = :productSkuId")
    void deleteByCartIdAndProductSkuId(
        @Param("cartId") Long cartId, @Param("productSkuId") Long productSkuId
    );
}
