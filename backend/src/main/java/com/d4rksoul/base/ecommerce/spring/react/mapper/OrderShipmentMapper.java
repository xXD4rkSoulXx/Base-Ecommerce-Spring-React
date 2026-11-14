package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderShipmentResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipmentItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.ShipmentTrackingEvent;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderShipmentMapper {

    private final OrderShipmentItemMapper orderShipmentItemMapper;
    private final ShipmentTrackingEventMapper shipmentTrackingEventMapper;

    public OrderShipment toEntity(Order order, ShippoTransactionResponse transaction) {
        return OrderShipment.builder()
            .order(order)
            .shippoTransactionId(transaction.objectId())
            .trackingNumber(transaction.trackingNumber())
            .status(ShipmentStatus.PRE_TRANSIT)
            .build();
    }

    public OrderShipmentResponse toDto(
        OrderShipment shipment, List<OrderShipmentItem> items, List<ShipmentTrackingEvent> events
    ) {
        return OrderShipmentResponse.builder()
            .id(shipment.getId())
            .trackingNumber(shipment.getTrackingNumber())
            .status(shipment.getStatus())
            .items(items.stream().map(orderShipmentItemMapper::toDto).toList())
            .events(events.stream().map(shipmentTrackingEventMapper::toDto).toList())
            .build();
    }
}
