package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.CheckoutAddressRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderReturnResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderShipmentResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderStatusEventResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderSummaryResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ShippingRateResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
public class OrderMapper {

    public Order toEntity(
        User user,
        DiscountCode discountCode,
        String currency,
        CheckoutAddressRequest address,
        ShippingRateResponse rate
    ) {
        return Order.builder()
            .user(user)
            .status(OrderStatus.PENDING)
            .discountCode(discountCode)
            .discountPercentage(discountCode != null ? discountCode.getDiscountPercentage() : 0)
            .currency(currency)
            .taxAmount(BigDecimal.ZERO)
            .shippingCost(rate.amount())
            .recipientName(address.recipientName())
            .addressLine1(address.addressLine1())
            .addressLine2(address.addressLine2())
            .city(address.city())
            .state(address.state())
            .postalCode(address.postalCode())
            .country(address.country())
            .phone(address.phone())
            .shippingCarrier(rate.carrier())
            .shippingService(rate.service())
            .build();
    }

    public OrderSummaryResponse toSummaryDto(Order order) {
        return OrderSummaryResponse.builder()
            .id(order.getId())
            .status(order.getStatus())
            .createdAt(order.getCreatedAt())
            .totalAmount(order.getTotalAmount())
            .currency(order.getCurrency())
            .build();
    }

    public OrderDetailsResponse toDetailsDto(
        Order order,
        List<OrderItemResponse> items,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        List<OrderStatusEventResponse> events,
        List<OrderShipmentResponse> shipments,
        List<OrderReturnResponse> returns,
        Instant returnableUntil
    ) {
        return OrderDetailsResponse.builder()
            .id(order.getId())
            .status(order.getStatus())
            .createdAt(order.getCreatedAt())
            .currency(order.getCurrency())
            .discountCode(order.getDiscountCode() != null ? order.getDiscountCode().getCode() : null)
            .subtotal(subtotal)
            .discountAmount(discountAmount)
            .shippingCost(order.getShippingCost())
            .taxAmount(order.getTaxAmount())
            .totalAmount(order.getTotalAmount())
            .shippingCarrier(order.getShippingCarrier())
            .shippingService(order.getShippingService())
            .recipientName(order.getRecipientName())
            .addressLine1(order.getAddressLine1())
            .addressLine2(order.getAddressLine2())
            .city(order.getCity())
            .state(order.getState())
            .postalCode(order.getPostalCode())
            .country(order.getCountry())
            .phone(order.getPhone())
            .events(events)
            .shipments(shipments)
            .returnableUntil(returnableUntil)
            .returns(returns)
            .items(items)
            .build();
    }
}
