package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ReturnedQuantityProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.CreateReturnItemRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.CreateReturnRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderReturnResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturnItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderStatusEvent;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnReason;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidReturnRequestException;
import com.d4rksoul.base.ecommerce.spring.react.exception.OrderNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ReturnNotAllowedException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderReturnItemMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderReturnMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderStatusEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderReturnService {

    private final OrderRepository orderRepository;
    private final OrderReturnRepository orderReturnRepository;
    private final OrderReturnItemRepository orderReturnItemRepository;
    private final OrderStatusEventRepository orderStatusEventRepository;
    private final UserService userService;
    private final OrderStatusEventService orderStatusEventService;
    private final OrderReturnMapper orderReturnMapper;
    private final OrderReturnItemMapper orderReturnItemMapper;

    @Transactional
    public OrderReturnResponse requestReturn(Long orderId, CreateReturnRequest request) {
        String comment = StringUtils.hasText(request.comment()) ? request.comment().trim() : null;
        if (request.reason() == ReturnReason.OTHER && comment == null) {
            throw new InvalidReturnRequestException("Please describe the reason for the return.");
        }

        User user = userService.getCurrentUser();

        Order order = orderRepository
            .findByIdAndUserIdAndStatusIn(orderId, user.getId(), List.of(OrderStatus.DELIVERED))
            .orElseThrow(() -> new OrderNotFoundException("No delivered order found with id: " + orderId));

        Instant deliveredAt = orderStatusEventRepository
            .findFirstByOrderIdAndStatusOrderByOccurredAtAsc(
                orderId, OrderStatus.DELIVERED
            )
            .map(OrderStatusEvent::getOccurredAt)
            .orElseThrow(() -> new ReturnNotAllowedException("The delivery date of this order is unknown."));

        if (Instant.now().isAfter(deliveredAt.plus(Constants.RETURN_WINDOW))) {
            throw new ReturnNotAllowedException("The 14-day return period for this order has expired.");
        }

        Map<Long, OrderItem> orderItemsById = order.getOrderItems().stream()
            .collect(Collectors.toMap(OrderItem::getId, Function.identity()));

        Map<Long, Long> returnedByItemId = orderReturnItemRepository
            .sumReturnedQuantities(orderId, ReturnStatus.INACTIVE_STATUSES)
            .stream()
            .collect(Collectors.toMap(
                ReturnedQuantityProjection::orderItemId,
                ReturnedQuantityProjection::quantity
            ));

        Set<Long> seen = new HashSet<>();

        for (CreateReturnItemRequest item : request.items()) {
            OrderItem orderItem = orderItemsById.get(item.orderItemId());

            if (orderItem == null) {
                throw new InvalidReturnRequestException(
                    "Item " + item.orderItemId() + " does not belong to this order."
                );
            }

            if (!seen.add(item.orderItemId())) {
                throw new InvalidReturnRequestException("Each item can only appear once in a return.");
            }

            long available = orderItem.getQuantity()
                - returnedByItemId.getOrDefault(item.orderItemId(), 0L);
            if (item.quantity() > available) {
                throw new InvalidReturnRequestException(
                    "Cannot return " + item.quantity() + " of " + orderItem.getProductName()
                        + ", only " + available + " left to return."
                );
            }
        }

        boolean transitioned = orderStatusEventService
            .transition(orderId, OrderStatus.DELIVERED, OrderStatus.RETURN_REQUESTED);
        if (!transitioned) {
            throw new ReturnNotAllowedException("This order is no longer eligible for return.");
        }

        OrderReturn orderReturn = orderReturnMapper.toEntity(order, request.reason(), comment);

        orderReturnRepository.save(orderReturn);

        List<OrderReturnItem> returnItems = orderReturnItemRepository.saveAll(
            request.items().stream()
                .map(item -> orderReturnItemMapper.toEntity(
                    orderReturn,
                    orderItemsById.get(item.orderItemId()),
                    item.quantity()
                ))
                .toList()
        );

        return orderReturnMapper.toDto(orderReturn, returnItems);
    }
}
