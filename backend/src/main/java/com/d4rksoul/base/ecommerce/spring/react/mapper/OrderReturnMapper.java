package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderReturnResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturnItem;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnReason;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderReturnMapper {

    private final OrderReturnItemMapper orderReturnItemMapper;

    public OrderReturn toEntity(Order order, ReturnReason reason, String comment) {
        return OrderReturn.builder()
                .order(order)
                .status(ReturnStatus.REQUESTED)
                .reason(reason)
                .comment(comment)
                .build();
    }

    public OrderReturnResponse toDto(OrderReturn orderReturn, List<OrderReturnItem> items) {
        return OrderReturnResponse.builder()
                .id(orderReturn.getId())
                .status(orderReturn.getStatus())
                .reason(orderReturn.getReason())
                .comment(orderReturn.getComment())
                .refundAmount(orderReturn.getRefundAmount())
                .createdAt(orderReturn.getCreatedAt())
                .refundedAt(orderReturn.getRefundedAt())
                .items(items.stream().map(orderReturnItemMapper::toDto).toList())
                .build();
    }

    public List<OrderReturnResponse> toDtos(List<OrderReturn> returns, List<OrderReturnItem> items) {
        Map<Long, List<OrderReturnItem>> itemsByReturnId = items.stream()
                .collect(Collectors.groupingBy(item -> item.getOrderReturn().getId()));

        return returns.stream()
                .map(orderReturn -> toDto(
                        orderReturn,
                        itemsByReturnId.getOrDefault(orderReturn.getId(), List.of())
                )).toList();
    }
}
