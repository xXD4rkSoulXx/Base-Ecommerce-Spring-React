package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import org.springframework.stereotype.Component;

@Component
public class OrderItemMapper {

    public OrderItem toEntity(Order order, CartItem cartItem, String imagePath) {
        ProductSku sku = cartItem.getProductSku();
        Product product = sku.getProduct();

        int discountPercentage = sku.getDiscountPercentage() > 0
                ? sku.getDiscountPercentage()
                : product.getDiscountPercentage();

        return OrderItem.builder()
                .order(order)
                .productSku(sku)
                .quantity(cartItem.getQuantity())
                .price(product.getPrice())
                .discountPercentage(discountPercentage)
                .productName(product.getName())
                .color(String.valueOf(sku.getColor()))
                .size(String.valueOf(sku.getSize()))
                .imagePath(imagePath)
                .build();
    }
}
