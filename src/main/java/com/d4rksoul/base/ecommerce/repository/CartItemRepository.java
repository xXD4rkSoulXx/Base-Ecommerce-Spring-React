package com.d4rksoul.base.ecommerce.repository;

import com.d4rksoul.base.ecommerce.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<List<CartItem>> findAllByCartId(Long cartId);
}
