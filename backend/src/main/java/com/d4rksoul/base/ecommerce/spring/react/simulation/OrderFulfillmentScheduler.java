package com.d4rksoul.base.ecommerce.spring.react.simulation;

import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.ShipmentTrackingEvent;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ShipmentTrackingEventRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderFulfillmentService;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderStatusEventService;
import com.d4rksoul.base.ecommerce.spring.react.service.ShipmentTrackingEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@ConditionalOnProperty(prefix = "app.simulation", name = "enabled", havingValue = "true")
@Component
@RequiredArgsConstructor
public class OrderFulfillmentScheduler {

    private static final Duration PROCESSING_DELAY = Duration.ofMinutes(1);
    private static final Duration SHIPPED_DELAY = Duration.ofMinutes(5);
    private static final Duration DELIVERED_DELAY = Duration.ofMinutes(15);

    private static final Duration STAGE_MIN = Duration.ofMinutes(5);
    private static final Duration STAGE_MAX = Duration.ofMinutes(15);
    private static final double STAGE_ADVANCE_CHANCE = 0.1;

    private final OrderRepository orderRepository;
    private final ShipmentTrackingEventRepository shipmentTrackingEventRepository;
    private final OrderFulfillmentService orderFulfillmentService;
    private final OrderStatusEventService orderStatusEventService;
    private final ShipmentTrackingEventService shipmentTrackingEventService;

    @Scheduled(fixedDelay = 30_000)
    public void advanceOrders() {
        startProcessing();

        advanceShipments(ShipmentStatus.PRE_TRANSIT, ShipmentStatus.IN_TRANSIT);
        advanceShipments(ShipmentStatus.IN_TRANSIT, ShipmentStatus.OUT_FOR_DELIVERY);
        advanceShipments(ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.DELIVERED);

        syncOrdersWithShipments();

        advanceOrdersWithoutShipments(OrderStatus.PROCESSING, OrderStatus.SHIPPED, SHIPPED_DELAY);
        advanceOrdersWithoutShipments(OrderStatus.SHIPPED, OrderStatus.DELIVERED, DELIVERED_DELAY);
    }

    private void startProcessing() {
        Instant limit = Instant.now().minus(PROCESSING_DELAY);

        for (Order order : orderRepository
                .findByStatusAndPaidAtBeforeAndManuallyManagedFalse(OrderStatus.PAID, limit)
        ) {
            orderFulfillmentService.startProcessing(order.getId());
        }
    }

    private void advanceShipments(ShipmentStatus from, ShipmentStatus to) {
        Instant now = Instant.now();

        for (ShipmentTrackingEvent event : shipmentTrackingEventRepository
                .findCurrentByStatusAndOccurredAtBefore(from, now.minus(STAGE_MIN))) {
            boolean overdue = Duration.between(event.getOccurredAt(), now).compareTo(STAGE_MAX) >= 0;

            if (overdue || ThreadLocalRandom.current().nextDouble() < STAGE_ADVANCE_CHANCE) {
                shipmentTrackingEventService.transition(event.getShipment().getId(), from, to);
            }
        }
    }

    private void syncOrdersWithShipments() {
        for (Order order : orderRepository.findWithAnyShipmentOtherThan(
                OrderStatus.PROCESSING, ShipmentStatus.PRE_TRANSIT)) {
            orderStatusEventService.transition(order.getId(), OrderStatus.PROCESSING, OrderStatus.SHIPPED);
        }

        for (Order order : orderRepository.findWithAllShipmentsIn(OrderStatus.SHIPPED, ShipmentStatus.DELIVERED)) {
            orderStatusEventService.transition(order.getId(), OrderStatus.SHIPPED, OrderStatus.DELIVERED);
        }
    }

    private void advanceOrdersWithoutShipments(OrderStatus from, OrderStatus to, Duration delay) {
        Instant limit = Instant.now().minus(delay);

        for (Order order : orderRepository.findByStatusAndPaidAtBeforeWithoutShipments(from, limit)) {
            orderStatusEventService.transition(order.getId(), from, to);
        }
    }
}
