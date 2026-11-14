package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderReturnItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturnItem;
import org.springframework.stereotype.Component;

@Component
public class OrderReturnItemMapper {

    public OrderReturnItem toEntity(OrderReturn orderReturn, OrderItem orderItem, int quantity) {
        return OrderReturnItem.builder()
            .orderReturn(orderReturn)
            .orderItem(orderItem)
            .quantity(quantity)
            .build();
    }

    public OrderReturnItemResponse toDto(OrderReturnItem item) {
        return OrderReturnItemResponse.builder()
            .orderItemId(item.getOrderItem().getId())
            .quantity(item.getQuantity())
            .build();
    }
}
