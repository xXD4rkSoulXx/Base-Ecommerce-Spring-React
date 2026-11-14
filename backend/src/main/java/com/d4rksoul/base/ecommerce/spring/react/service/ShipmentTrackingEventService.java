package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.config.ShippingOriginProperties;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.entity.ShipmentTrackingEvent;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import com.d4rksoul.base.ecommerce.spring.react.mapper.ShipmentTrackingEventMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderShipmentRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ShipmentTrackingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShipmentTrackingEventService {

    private static final Map<ShipmentStatus, String> DESCRIPTIONS = Map.of(
            ShipmentStatus.IN_TRANSIT, "Parcel picked up by the carrier",
            ShipmentStatus.OUT_FOR_DELIVERY, "Out for delivery",
            ShipmentStatus.FAILURE, "Delivery attempt failed",
            ShipmentStatus.DELIVERED, "Delivered"
    );

    private final OrderShipmentRepository orderShipmentRepository;
    private final ShipmentTrackingEventRepository shipmentTrackingEventRepository;
    private final ShipmentTrackingEventMapper shipmentTrackingEventMapper;
    private final ShippingOriginProperties originProperties;

    @Transactional
    public boolean transition(Long shipmentId, ShipmentStatus from, ShipmentStatus to) {
        String description = Optional.ofNullable(DESCRIPTIONS.get(to))
            .orElseThrow(() -> new IllegalArgumentException("No tracking description for status " + to));

        int updatedRows = orderShipmentRepository.updateStatus(shipmentId, from, to);
        if (updatedRows == 0) {
            return false;
        }

        OrderShipment shipment = orderShipmentRepository.getReferenceById(shipmentId);

        String location = to == ShipmentStatus.IN_TRANSIT
            ? originProperties.city()
            : shipment.getOrder().getCity();

        ShipmentTrackingEvent trackingEvent = shipmentTrackingEventMapper.toEntity(
            shipment, to, description, location, Instant.now()
        );

        shipmentTrackingEventRepository.save(trackingEvent);

        return true;
    }
}
