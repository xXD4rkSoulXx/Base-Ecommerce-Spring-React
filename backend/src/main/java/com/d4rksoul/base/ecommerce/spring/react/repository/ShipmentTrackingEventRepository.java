package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.entity.ShipmentTrackingEvent;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ShipmentTrackingEventRepository
    extends JpaRepository<ShipmentTrackingEvent, Long> {

    @Query("""
        SELECT e FROM ShipmentTrackingEvent e
        JOIN FETCH e.shipment s
        WHERE e.status = :status
        AND s.status = :status
        AND e.occurredAt < :limit
        AND s.order.manuallyManaged = false
    """)
    List<ShipmentTrackingEvent> findCurrentByStatusAndOccurredAtBefore(
        @Param("status") ShipmentStatus status, @Param("limit") Instant limit
    );

    List<ShipmentTrackingEvent> findByShipmentInOrderByOccurredAtAscIdAsc(
        List<OrderShipment> shipments
    );
}
