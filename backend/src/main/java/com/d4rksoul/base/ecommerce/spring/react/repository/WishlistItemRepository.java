package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    boolean existsByWishlistIdAndProductId(Long wishlistId, Long productId);

    List<WishlistItem> findByWishlistId(Long wishlistId);

    @Query("SELECT wi.product.id FROM WishlistItem wi WHERE wi.wishlist.id = :wishlistId")
    List<Long> findProductIdsByWishlistId(@Param("wishlistId") Long wishlistId);

    void deleteByWishlistId(Long wishlistId);

    void deleteByWishlistIdAndProductId(Long wishlistId, Long productId);
}
