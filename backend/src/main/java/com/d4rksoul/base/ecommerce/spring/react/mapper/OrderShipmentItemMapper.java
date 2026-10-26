package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderShipmentItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackingItemResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipmentItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OrderShipmentItemMapper {

    public List<OrderShipmentItem> toEntities(OrderShipment shipment, Order order, List<PackingItemResult> items) {
        Map<Long, OrderItem> orderItemsBySkuId = order.getOrderItems().stream()
                .collect(Collectors.toMap(item -> item.getProductSku().getId(), Function.identity()));

        return items.stream()
                .collect(Collectors.groupingBy(
                        PackingItemResult::productSkuId, LinkedHashMap::new, Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> OrderShipmentItem.builder()
                        .shipment(shipment)
                        .orderItem(orderItemsBySkuId.get(entry.getKey()))
                        .quantity(entry.getValue().intValue())
                        .build())
                .toList();
    }

    public OrderShipmentItemResponse toDto(OrderShipmentItem item) {
        return OrderShipmentItemResponse.builder()
                .orderItemId(item.getOrderItem().getId())
                .quantity(item.getQuantity())
                .build();
    }
}
