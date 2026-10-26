package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderShipmentRepository extends JpaRepository<OrderShipment, Long> {

    List<OrderShipment> findByOrderIdOrderByIdAsc(Long orderId);

    @Modifying
    @Transactional
    @Query("update OrderShipment s set s.status = :to where s.id = :id and s.status = :from")
    int updateStatus(@Param("id") Long id, @Param("from") ShipmentStatus from, @Param("to") ShipmentStatus to);
}
