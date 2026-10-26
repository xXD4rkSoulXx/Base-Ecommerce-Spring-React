package com.d4rksoul.base.ecommerce.spring.react.repository;

import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderShipmentItemRepository extends JpaRepository<OrderShipmentItem, Long> {

    List<OrderShipmentItem> findByShipmentInOrderByIdAsc(List<OrderShipment> shipments);
}
