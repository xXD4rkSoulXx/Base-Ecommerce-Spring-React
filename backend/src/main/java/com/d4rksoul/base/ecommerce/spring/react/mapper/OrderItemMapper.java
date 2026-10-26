package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    public OrderItemResponse toDto(OrderItem item, int returnableQuantity) {
        BigDecimal unitPrice = toUnitPrice(item);

        return OrderItemResponse.builder()
                .id(item.getId())
                .ref(item.getProductSku().getProduct().getRef())
                .productName(item.getProductName())
                .color(item.getColor())
                .size(item.getSize())
                .imagePath(item.getImagePath())
                .quantity(item.getQuantity())
                .unitPrice(unitPrice)
                .lineTotal(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())))
                .returnableQuantity(returnableQuantity)
                .build();
    }

    public BigDecimal toUnitPrice(OrderItem item) {
        return item.getPrice()
                .multiply(BigDecimal.valueOf(100L - item.getDiscountPercentage()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
