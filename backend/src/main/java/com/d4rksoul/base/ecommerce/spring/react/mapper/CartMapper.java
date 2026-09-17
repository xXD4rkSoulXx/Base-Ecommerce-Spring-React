package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.CartItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.CartResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Cart;
import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
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

    public CartItem toEntity(Cart cart, ProductSku productSku, int quantity) {
        return CartItem.builder()
                .cart(cart)
                .productSku(productSku)
                .quantity(quantity)
                .build();
    }

    public CartItemResponse toDto(CartItem cartItem, String cover) {
        if (cartItem == null) {
            return null;
        }

        ProductSku sku = cartItem.getProductSku();
        Product product = sku.getProduct();

        int effectiveDiscountPercentage = sku.getDiscountPercentage() > 0
                ? sku.getDiscountPercentage()
                : product.getDiscountPercentage();

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productSkuId(sku.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productRef(product.getRef())
                .size(String.valueOf(sku.getSize()))
                .color(String.valueOf(sku.getColor()))
                .cover(cover)
                .price(product.getPrice())
                .discountPercentage(effectiveDiscountPercentage)
                .stock(sku.getStock())
                .quantity(cartItem.getQuantity())
                .build();
    }

    public CartResponse toCartDto(List<CartItemResponse> items, DiscountCode discountCode) {
        return CartResponse.builder()
                .items(items)
                .discountCode(discountCode != null ? discountCode.getCode() : null)
                .discountPercentage(discountCode != null ? discountCode.getDiscountPercentage() : 0)
                .build();
    }
}
