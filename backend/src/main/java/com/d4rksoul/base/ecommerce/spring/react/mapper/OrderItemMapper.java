package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class OrderItemMapper {

    public OrderItem toEntity(Order order, CartItem cartItem, String imagePath) {
        return OrderItem.builder()
            .order(order)
            .productSku(cartItem.getProductSku())
            .quantity(cartItem.getQuantity())
            .price(cartItem.getProductSku().getProduct().getPrice())
            .discountPercentage(
                cartItem.getProductSku().getDiscountPercentage() > 0
                    ? cartItem.getProductSku().getDiscountPercentage()
                    : cartItem.getProductSku().getProduct().getDiscountPercentage()
            )
            .productName(cartItem.getProductSku().getProduct().getName())
            .color(cartItem.getProductSku().getColor())
            .size(cartItem.getProductSku().getSize())
            .imagePath(imagePath)
            .weightGrams(cartItem.getProductSku().getWeightGrams())
            .lengthCm(cartItem.getProductSku().getLengthCm())
            .widthCm(cartItem.getProductSku().getWidthCm())
            .heightCm(cartItem.getProductSku().getHeightCm())
            .build();
    }

    public OrderItemResponse toDto(OrderItem item, int returnableQuantity) {
        return OrderItemResponse.builder()
            .id(item.getId())
            .ref(item.getProductSku().getProduct().getRef())
            .productName(item.getProductName())
            .color(item.getColor())
            .size(item.getSize())
            .imagePath(item.getImagePath())
            .quantity(item.getQuantity())
            .unitPrice(
                item.getPrice()
                    .multiply(BigDecimal.valueOf(100L - item.getDiscountPercentage()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            )
            .lineTotal(
                item.getPrice()
                    .multiply(BigDecimal.valueOf(100L - item.getDiscountPercentage()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
            )
            .returnableQuantity(returnableQuantity)
            .build();
    }
}
