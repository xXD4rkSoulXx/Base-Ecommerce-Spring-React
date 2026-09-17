package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.entity.Wishlist;
import com.d4rksoul.base.ecommerce.spring.react.entity.WishlistItem;
import org.springframework.stereotype.Component;

@Component
public class WishlistMapper {

    public Wishlist toEntity(User user) {
        return Wishlist.builder()
                .user(user)
                .build();
    }

    public WishlistItem toEntity(Wishlist wishlist, Product product) {
        return WishlistItem.builder()
                .wishlist(wishlist)
                .product(product)
                .build();
    }
}
