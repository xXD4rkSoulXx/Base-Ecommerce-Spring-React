package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.CartItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.CartResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Cart;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartMapper {

    public Cart toEntity(User user) {
        return Cart.builder()
            .user(user)
            .build();
    }

    public CartResponse toDto(List<CartItemResponse> items, DiscountCode discountCode) {
        return CartResponse.builder()
            .items(items)
            .discountCode(discountCode != null ? discountCode.getCode() : null)
            .discountPercentage(discountCode != null ? discountCode.getDiscountPercentage() : 0)
            .build();
    }
}
