package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderStatusEvent;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderStatusEventMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderStatusEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OrderStatusEventService {

    private final OrderRepository orderRepository;
    private final OrderStatusEventRepository orderStatusEventRepository;
    private final OrderStatusEventMapper orderStatusEventMapper;

    @Transactional
    public boolean transition(Long orderId, OrderStatus from, OrderStatus to) {
        int updatedRows = orderRepository.updateStatus(orderId, from, to);
        if (updatedRows == 0) {
            return false;
        }

        Order order = orderRepository.getReferenceById(orderId);

        OrderStatusEvent statusEvent = orderStatusEventMapper.toEntity(order, to, Instant.now());

        orderStatusEventRepository.save(statusEvent);

        return true;
    }
}
