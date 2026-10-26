package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.CheckoutAddressRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderReturnResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderShipmentResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderStatusEventResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderSummaryResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ShippingRateResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.ReturnEligibilityResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipmentItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderStatusEvent;
import com.d4rksoul.base.ecommerce.spring.react.entity.ShipmentTrackingEvent;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final OrderItemMapper orderItemMapper;
    private final OrderShipmentMapper orderShipmentMapper;
    private final OrderStatusEventMapper orderStatusEventMapper;

    public Order toEntity(
            User user,
            DiscountCode discountCode,
            String currency,
            CheckoutAddressRequest address,
            ShippingRateResponse rate,
            List<CartItem> cartItems,
            Map<Long, String> covers
    ) {
        Order order = Order.builder()
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

        for (CartItem cartItem : cartItems) {
            String imagePath = covers.get(cartItem.getProductSku().getId());

            order.getOrderItems().add(orderItemMapper.toEntity(order, cartItem, imagePath));
        }

        return order;
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
            List<OrderShipment> shipments,
            List<OrderShipmentItem> shipmentItems,
            List<ShipmentTrackingEvent> shipmentEvents,
            List<OrderStatusEvent> events,
            List<OrderReturnResponse> returns,
            ReturnEligibilityResult eligibility
    ) {
        List<OrderShipmentResponse> shipmentResponses =
                orderShipmentMapper.toDtos(shipments, shipmentItems, shipmentEvents);

        List<OrderStatusEventResponse> eventResponses = events.stream()
                .map(orderStatusEventMapper::toDto)
                .toList();

        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> orderItemMapper.toDto(
                        item,
                        eligibility.returnableByItemId().getOrDefault(item.getId(), 0L).intValue()
                ))
                .toList();

        BigDecimal subtotal = items.stream()
                .map(OrderItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = subtotal
                .add(order.getShippingCost())
                .add(order.getTaxAmount())
                .subtract(order.getTotalAmount());

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
                .events(eventResponses)
                .shipments(shipmentResponses)
                .returnableUntil(eligibility.returnableUntil())
                .returns(returns)
                .items(items)
                .build();
    }
}
