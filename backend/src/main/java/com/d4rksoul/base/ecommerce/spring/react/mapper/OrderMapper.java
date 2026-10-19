package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.CheckoutAddressRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ShippingRateResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
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
}
