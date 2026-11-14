package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderShipmentItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipmentItem;
import org.springframework.stereotype.Component;

@Component
public class OrderShipmentItemMapper {

    public OrderShipmentItem toEntity(OrderShipment shipment, OrderItem orderItem, int quantity) {
        return OrderShipmentItem.builder()
            .shipment(shipment)
            .orderItem(orderItem)
            .quantity(quantity)
            .build();
    }

    public OrderShipmentItemResponse toDto(OrderShipmentItem item) {
        return OrderShipmentItemResponse.builder()
            .orderItemId(item.getOrderItem().getId())
            .quantity(item.getQuantity())
            .build();
    }
}
