package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderStatusEventResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderStatusEvent;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OrderStatusEventMapper {

    public OrderStatusEvent toEntity(Order order, OrderStatus status, Instant occurredAt) {
        return OrderStatusEvent.builder()
            .order(order)
            .status(status)
            .occurredAt(occurredAt)
            .build();
    }

    public OrderStatusEventResponse toDto(OrderStatusEvent event) {
        return OrderStatusEventResponse.builder()
            .status(event.getStatus())
            .occurredAt(event.getOccurredAt())
            .build();
    }
}
