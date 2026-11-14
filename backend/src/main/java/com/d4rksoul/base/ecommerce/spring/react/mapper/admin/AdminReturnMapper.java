package com.d4rksoul.base.ecommerce.spring.react.mapper.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminReturnAddressResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminReturnItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminReturnResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturnItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminReturnMapper {

    public AdminReturnResponse toDto(OrderReturn orderReturn, List<OrderReturnItem> items) {
        return AdminReturnResponse.builder()
            .id(orderReturn.getId())
            .orderId(orderReturn.getOrder().getId())
            .customerEmail(orderReturn.getOrder().getUser().getEmail())
            .address(toAddressDto(orderReturn.getOrder()))
            .status(orderReturn.getStatus())
            .reason(orderReturn.getReason())
            .comment(orderReturn.getComment())
            .refundAmount(orderReturn.getRefundAmount())
            .rejectionReason(orderReturn.getRejectionReason())
            .createdAt(orderReturn.getCreatedAt())
            .refundedAt(orderReturn.getRefundedAt())
            .items(items.stream().map(this::toItemDto).toList())
            .build();
    }

    private AdminReturnItemResponse toItemDto(OrderReturnItem item) {
        return AdminReturnItemResponse.builder()
            .ref(item.getOrderItem().getProductSku().getProduct().getRef())
            .productName(item.getOrderItem().getProductName())
            .color(item.getOrderItem().getColor())
            .size(item.getOrderItem().getSize())
            .imagePath(item.getOrderItem().getImagePath())
            .quantity(item.getQuantity())
            .build();
    }

    private AdminReturnAddressResponse toAddressDto(Order order) {
        return AdminReturnAddressResponse.builder()
            .recipientName(order.getRecipientName())
            .addressLine1(order.getAddressLine1())
            .addressLine2(order.getAddressLine2())
            .city(order.getCity())
            .state(order.getState())
            .postalCode(order.getPostalCode())
            .country(order.getCountry())
            .phone(order.getPhone())
            .build();
    }
}
