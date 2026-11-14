package com.d4rksoul.base.ecommerce.spring.react.mapper.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminOrderDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminOrderSummaryResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AdminOrderMapper {

    public AdminOrderSummaryResponse toSummaryDto(Order order) {
        return AdminOrderSummaryResponse.builder()
            .id(order.getId())
            .status(order.getStatus())
            .createdAt(order.getCreatedAt())
            .totalAmount(order.getTotalAmount())
            .currency(order.getCurrency())
            .customerEmail(order.getUser().getEmail())
            .recipientName(order.getRecipientName())
            .build();
    }

    public AdminOrderDetailsResponse toDetailsDto(
        Order order,
        OrderDetailsResponse details,
        OrderStatus nextStatus,
        Map<Long, List<ShipmentStatus>> shipmentActions
    ) {
        return AdminOrderDetailsResponse.builder()
                .customerEmail(order.getUser().getEmail())
                .customerName(order.getUser().getFirstName() + " " + order.getUser().getLastName())
                .nextStatus(nextStatus)
                .shipmentActions(shipmentActions)
                .order(details)
                .build();
    }
}
