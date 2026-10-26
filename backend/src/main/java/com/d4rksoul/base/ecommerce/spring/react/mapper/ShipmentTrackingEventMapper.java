package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.ShipmentTrackingEventResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.entity.ShipmentTrackingEvent;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ShipmentTrackingEventMapper {

    public ShipmentTrackingEvent toEntity(
            OrderShipment shipment,
            ShipmentStatus status,
            String description,
            String location,
            Instant occurredAt
    ) {
        return ShipmentTrackingEvent.builder()
                .shipment(shipment)
                .status(status)
                .description(description)
                .location(location)
                .occurredAt(occurredAt)
                .build();
    }

    public ShipmentTrackingEventResponse toDto(ShipmentTrackingEvent event) {
        return ShipmentTrackingEventResponse.builder()
                .status(event.getStatus())
                .description(event.getDescription())
                .location(event.getLocation())
                .occurredAt(event.getOccurredAt())
                .build();
    }
}
