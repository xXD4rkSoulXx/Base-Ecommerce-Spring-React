package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.CartItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Cart;
import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import org.springframework.stereotype.Component;

@Component
public class CartItemMapper {

    public CartItem toEntity(Cart cart, ProductSku productSku, int quantity) {
        return CartItem.builder()
            .cart(cart)
            .productSku(productSku)
            .quantity(quantity)
            .build();
    }

    public CartItemResponse toDto(CartItem cartItem, String cover) {
        return CartItemResponse.builder()
            .id(cartItem.getId())
            .productSkuId(cartItem.getProductSku().getId())
            .productId(cartItem.getProductSku().getProduct().getId())
            .productName(cartItem.getProductSku().getProduct().getName())
            .productRef(cartItem.getProductSku().getProduct().getRef())
            .size(cartItem.getProductSku().getSize())
            .color(cartItem.getProductSku().getColor())
            .cover(cover)
            .price(cartItem.getProductSku().getProduct().getPrice())
            .discountPercentage(
                cartItem.getProductSku().getDiscountPercentage() > 0
                    ? cartItem.getProductSku().getDiscountPercentage()
                    : cartItem.getProductSku().getProduct().getDiscountPercentage()
            )
            .stock(cartItem.getProductSku().getStock())
            .quantity(cartItem.getQuantity())
            .available(
                cartItem.getProductSku().isActive()
                && cartItem.getProductSku().getProduct().isActive()
            )
            .build();
    }
}
