package com.d4rksoul.base.ecommerce.spring.react.simulation;

import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.ShipmentTrackingEvent;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ShipmentTrackingEventRepository;
import com.d4rksoul.base.ecommerce.spring.react.scheduler.RequestActivityFilter;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderShipmentService;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderStatusEventService;
import com.d4rksoul.base.ecommerce.spring.react.service.ShipmentTrackingEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@ConditionalOnProperty(prefix = "app.simulation", name = "enabled", havingValue = "true")
@Component
@RequiredArgsConstructor
public class OrderFulfillmentScheduler {
    private final OrderRepository orderRepository;
    private final ShipmentTrackingEventRepository shipmentTrackingEventRepository;
    private final OrderShipmentService orderShipmentService;
    private final OrderStatusEventService orderStatusEventService;
    private final ShipmentTrackingEventService shipmentTrackingEventService;
    private final RequestActivityFilter activityFilter;

    @Scheduled(fixedDelay = SimulationConstants.TICK_MS)
    public void advanceOrders() {
        if (!activityFilter.isActive()) {
            return;
        }

        Instant limit = Instant.now().minus(SimulationConstants.PAID_TO_PROCESSING_DELAY);

        List<Order> orders = orderRepository
            .findByStatusAndPaidAtBeforeAndManuallyManagedFalse(OrderStatus.PAID, limit);
        for (Order order : orders) {
            try {
                boolean transitioned = orderStatusEventService
                    .transition(order.getId(), OrderStatus.PAID, OrderStatus.PROCESSING);
                if (transitioned) {
                    orderShipmentService.purchaseLabels(order.getId());
                }
            } catch (RuntimeException _) {/**/}
        }

        advanceShipments(ShipmentStatus.PRE_TRANSIT, ShipmentStatus.IN_TRANSIT);
        advanceShipments(ShipmentStatus.IN_TRANSIT, ShipmentStatus.OUT_FOR_DELIVERY);
        advanceShipments(ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.DELIVERED);

        List<Order> ordersWithDepartedShipments = orderRepository
            .findWithAnyShipmentOtherThan(OrderStatus.PROCESSING, ShipmentStatus.PRE_TRANSIT);
        for (Order order : ordersWithDepartedShipments) {
            try {
                orderStatusEventService
                    .transition(order.getId(), OrderStatus.PROCESSING, OrderStatus.SHIPPED);
            } catch (RuntimeException _) {/**/}
        }

        List<Order> ordersWithDeliveredShipments = orderRepository
            .findWithAllShipmentsIn(OrderStatus.SHIPPED, ShipmentStatus.DELIVERED);
        for (Order order : ordersWithDeliveredShipments) {
            try {
                orderStatusEventService
                    .transition(order.getId(), OrderStatus.SHIPPED, OrderStatus.DELIVERED);
            } catch (RuntimeException _) {/**/}
        }

        advanceOrdersWithoutShipments(
            OrderStatus.PROCESSING, OrderStatus.SHIPPED, SimulationConstants.PROCESSING_TO_SHIPPED_DELAY
        );
        advanceOrdersWithoutShipments(
            OrderStatus.SHIPPED, OrderStatus.DELIVERED, SimulationConstants.SHIPPED_TO_DELIVERED_DELAY
        );
    }

    private void advanceShipments(ShipmentStatus from, ShipmentStatus to) {
        Instant now = Instant.now();

        List<ShipmentTrackingEvent> currentEvents = shipmentTrackingEventRepository
            .findCurrentByStatusAndOccurredAtBefore(
                from, now.minus(SimulationConstants.SHIPMENT_MIN_WAIT))
            ;
        for (ShipmentTrackingEvent event : currentEvents) {
            try {
                boolean waitedLongEnough = Duration.between(event.getOccurredAt(), now)
                    .compareTo(SimulationConstants.SHIPMENT_MAX_WAIT) >= 0;
                boolean advancedByChance = ThreadLocalRandom
                    .current().nextDouble() < SimulationConstants.SHIPMENT_ADVANCE_CHANCE;
                if (waitedLongEnough || advancedByChance) {
                    shipmentTrackingEventService.transition(event.getShipment().getId(), from, to);
                }
            } catch (RuntimeException _) {/**/}
        }
    }

    private void advanceOrdersWithoutShipments(
        OrderStatus from, OrderStatus to, Duration delay
    ) {
        Instant limit = Instant.now().minus(delay);

        List<Order> pendingOrders = orderRepository
            .findByStatusAndPaidAtBeforeWithoutShipments(from, limit);
        for (Order order : pendingOrders) {
            try {
                orderStatusEventService.transition(order.getId(), from, to);
            } catch (RuntimeException _) {/**/}
        }
    }
}
