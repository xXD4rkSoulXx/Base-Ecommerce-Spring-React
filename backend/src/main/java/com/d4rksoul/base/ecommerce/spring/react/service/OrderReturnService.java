package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.CreateReturnItemRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.CreateReturnRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderReturnResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.ReturnEligibilityResult;
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
import com.d4rksoul.base.ecommerce.spring.react.exception.UserNotAuthenticatedException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderReturnItemMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderReturnMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderStatusEventRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderReturnService {

    private static final Duration RETURN_WINDOW = Duration.ofDays(14);

    private final OrderRepository orderRepository;
    private final OrderReturnRepository orderReturnRepository;
    private final OrderReturnItemRepository orderReturnItemRepository;
    private final OrderStatusEventRepository orderStatusEventRepository;
    private final UserRepository userRepository;
    private final OrderStatusEventService orderStatusEventService;
    private final OrderReturnMapper orderReturnMapper;
    private final OrderReturnItemMapper orderReturnItemMapper;

    @Transactional
    public OrderReturnResponse requestReturn(Long orderId, CreateReturnRequest request) {
        String comment = StringUtils.hasText(request.comment()) ? request.comment().trim() : null;

        if (request.reason() == ReturnReason.OTHER && comment == null) {
            throw new InvalidReturnRequestException("Please describe the reason for the return.");
        }

        User user = getCurrentUser();

        Order order = orderRepository
                .findByIdAndUserIdAndStatusIn(orderId, user.getId(), List.of(OrderStatus.DELIVERED))
                .orElseThrow(() -> new OrderNotFoundException("No delivered order found with id: " + orderId));

        Instant deliveredAt = orderStatusEventRepository
                .findFirstByOrderIdAndStatusOrderByOccurredAtAsc(orderId, OrderStatus.DELIVERED)
                .map(OrderStatusEvent::getOccurredAt)
                .orElseThrow(() -> new ReturnNotAllowedException("The delivery date of this order is unknown."));

        if (Instant.now().isAfter(deliveredAt.plus(RETURN_WINDOW))) {
            throw new ReturnNotAllowedException("The 14-day return period for this order has expired.");
        }

        Map<Long, OrderItem> orderItemsById = order.getOrderItems().stream()
                .collect(Collectors.toMap(OrderItem::getId, Function.identity()));
        Map<Long, Long> returnedByItemId = orderReturnItemMapper.toReturnedQuantities(
                orderReturnItemRepository.sumReturnedQuantities(orderId, ReturnStatus.FAILED)
        );

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

            long available = orderItem.getQuantity() - returnedByItemId.getOrDefault(item.orderItemId(), 0L);

            if (item.quantity() > available) {
                throw new InvalidReturnRequestException(
                        "Cannot return " + item.quantity() + " of " + orderItem.getProductName()
                                + ", only " + available + " left to return."
                );
            }
        }

        if (!orderStatusEventService.transition(orderId, OrderStatus.DELIVERED, OrderStatus.RETURN_REQUESTED)) {
            throw new ReturnNotAllowedException("This order is no longer eligible for return.");
        }

        OrderReturn orderReturn = orderReturnRepository.save(
                orderReturnMapper.toEntity(order, request.reason(), comment)
        );

        List<OrderReturnItem> returnItems = orderReturnItemRepository.saveAll(
                orderReturnItemMapper.toEntities(orderReturn, orderItemsById, request.items())
        );

        return orderReturnMapper.toDto(orderReturn, returnItems);
    }

    @Transactional(readOnly = true)
    public List<OrderReturnResponse> getReturns(Long orderId) {
        List<OrderReturn> returns = orderReturnRepository.findByOrderIdOrderByCreatedAtAscIdAsc(orderId);
        List<OrderReturnItem> items = orderReturnItemRepository.findByOrderReturnInOrderByIdAsc(returns);

        return orderReturnMapper.toDtos(returns, items);
    }

    @Transactional(readOnly = true)
    public ReturnEligibilityResult getEligibility(Order order, List<OrderStatusEvent> events) {
        Instant deadline = events.stream()
                .filter(event -> event.getStatus() == OrderStatus.DELIVERED)
                .map(OrderStatusEvent::getOccurredAt)
                .findFirst()
                .map(deliveredAt -> deliveredAt.plus(RETURN_WINDOW))
                .orElse(null);

        if (order.getStatus() != OrderStatus.DELIVERED || deadline == null || Instant.now().isAfter(deadline)) {
            return new ReturnEligibilityResult(null, Map.of());
        }

        Map<Long, Long> returnedByItemId = orderReturnItemMapper.toReturnedQuantities(
                orderReturnItemRepository.sumReturnedQuantities(order.getId(), ReturnStatus.FAILED)
        );

        Map<Long, Long> returnableByItemId = order.getOrderItems().stream()
                .collect(Collectors.toMap(
                        OrderItem::getId,
                        item -> item.getQuantity() - returnedByItemId.getOrDefault(item.getId(), 0L)
                ));

        return new ReturnEligibilityResult(deadline, returnableByItemId);
    }

    private User getCurrentUser() {
        Jwt jwt = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> (Jwt) auth.getPrincipal())
                .orElseThrow(() -> new UserNotAuthenticatedException("User is not authenticated"));

        return userRepository.findByEmail(jwt.getSubject())
                .orElseThrow(() -> new UserNotAuthenticatedException("User is not authenticated"));
    }
}
