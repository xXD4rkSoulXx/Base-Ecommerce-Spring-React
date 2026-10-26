package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ReturnedQuantityProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.CreateReturnItemRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderReturnItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturnItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderReturnItemMapper {

    public List<OrderReturnItem> toEntities(
            OrderReturn orderReturn,
            Map<Long, OrderItem> orderItemsById,
            List<CreateReturnItemRequest> items
    ) {
        return items.stream()
                .map(item -> OrderReturnItem.builder()
                        .orderReturn(orderReturn)
                        .orderItem(orderItemsById.get(item.orderItemId()))
                        .quantity(item.quantity())
                        .build())
                .toList();
    }

    public OrderReturnItemResponse toDto(OrderReturnItem item) {
        return OrderReturnItemResponse.builder()
                .orderItemId(item.getOrderItem().getId())
                .quantity(item.getQuantity())
                .build();
    }

    public Map<Long, Long> toReturnedQuantities(List<ReturnedQuantityProjection> projections) {
        return projections.stream()
                .collect(Collectors.toMap(
                        ReturnedQuantityProjection::orderItemId,
                        ReturnedQuantityProjection::quantity
                ));
    }
}
