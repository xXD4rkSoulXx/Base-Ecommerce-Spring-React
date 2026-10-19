package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndProductSkuId(Long cartId, Long productSkuId);

    long countByCartId(Long cartId);

    void deleteByCartId(Long cartId);

    void deleteByCartIdAndProductSkuId(Long cartId, Long productSkuId);
}
