package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.config.ShippingOriginProperties;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import com.d4rksoul.base.ecommerce.spring.react.mapper.ShipmentTrackingEventMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderShipmentRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ShipmentTrackingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

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
        if (orderShipmentRepository.updateStatus(shipmentId, from, to) == 0) {
            return false;
        }

        OrderShipment shipment = orderShipmentRepository.getReferenceById(shipmentId);
        String location = to == ShipmentStatus.IN_TRANSIT
                ? originProperties.getCity()
                : shipment.getOrder().getCity();

        shipmentTrackingEventRepository.save(
                shipmentTrackingEventMapper.toEntity(shipment, to, DESCRIPTIONS.get(to), location, Instant.now())
        );

        return true;
    }
}
