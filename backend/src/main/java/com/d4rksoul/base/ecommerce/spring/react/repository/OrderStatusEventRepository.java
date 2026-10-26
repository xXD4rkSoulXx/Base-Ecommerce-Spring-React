package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.OrderStatusEvent;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderStatusEventRepository extends JpaRepository<OrderStatusEvent, Long> {

    List<OrderStatusEvent> findByOrderIdOrderByOccurredAtAscIdAsc(Long orderId);

    Optional<OrderStatusEvent> findFirstByOrderIdAndStatusOrderByOccurredAtAsc(Long orderId, OrderStatus status);
}
