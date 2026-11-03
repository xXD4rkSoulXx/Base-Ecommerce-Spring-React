package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderFulfillmentService {

    private final OrderShipmentRepository orderShipmentRepository;
    private final OrderShipmentService orderShipmentService;
    private final OrderStatusEventService orderStatusEventService;
    private final ShipmentTrackingEventService shipmentTrackingEventService;

    public boolean startProcessing(Long orderId) {
        if (!orderStatusEventService.transition(orderId, OrderStatus.PAID, OrderStatus.PROCESSING)) {
            return false;
        }

        orderShipmentService.purchaseLabels(orderId);

        return true;
    }

    @Transactional
    public boolean advanceShipment(Long orderId, Long shipmentId, ShipmentStatus from, ShipmentStatus to) {
        if (!shipmentTrackingEventService.transition(shipmentId, from, to)) {
            return false;
        }

        if (to == ShipmentStatus.IN_TRANSIT) {
            orderStatusEventService.transition(orderId, OrderStatus.PROCESSING, OrderStatus.SHIPPED);
        }

        if (to == ShipmentStatus.DELIVERED
                && !orderShipmentRepository.existsByOrderIdAndStatusNot(orderId, ShipmentStatus.DELIVERED)) {
            orderStatusEventService.transition(orderId, OrderStatus.SHIPPED, OrderStatus.DELIVERED);
        }

        return true;
    }
}
