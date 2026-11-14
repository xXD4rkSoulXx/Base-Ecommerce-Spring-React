package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.config.ShippingOriginProperties;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackedParcelItemsResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackingItemResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipmentItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.ShipmentTrackingEvent;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.ShippoApiException;
import com.d4rksoul.base.ecommerce.spring.react.exception.UnpackableCartException;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.ShippoApiClient;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoAddress;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoRate;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoShipmentRequest;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoShipmentResponse;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoTransactionRequest;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoTransactionResponse;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderShipmentItemMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderShipmentMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.ShipmentTrackingEventMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.ShippingMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderShipmentItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderShipmentRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ShipmentTrackingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderShipmentService {

    private final OrderRepository orderRepository;
    private final OrderShipmentRepository orderShipmentRepository;
    private final OrderShipmentItemRepository orderShipmentItemRepository;
    private final ShipmentTrackingEventRepository shipmentTrackingEventRepository;
    private final ParcelPackingService parcelPackingService;
    private final ShippingMapper shippingMapper;
    private final OrderShipmentMapper orderShipmentMapper;
    private final OrderShipmentItemMapper orderShipmentItemMapper;
    private final ShipmentTrackingEventMapper shipmentTrackingEventMapper;
    private final ShippoApiClient shippoApiClient;
    private final ShippingOriginProperties originProperties;

    @Transactional
    public void purchaseLabels(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);

        if (order == null) {
            return;
        }

        try {
            List<PackingItemResult> packingItems = buildPackingItems(order);

            List<PackedParcelItemsResult> parcels = parcelPackingService.pack(packingItems);
            ShippoAddress addressFrom = shippingMapper.toShippoAddress(originProperties);
            ShippoAddress addressTo = shippingMapper.toShippoAddress(order);
            List<PurchasedLabel> labels = new ArrayList<>();

            for (PackedParcelItemsResult packed : parcels) {
                ShippoShipmentRequest shipmentRequest = shippingMapper.toShipmentRequest(
                    addressFrom, addressTo, packed.parcel()
                );

                ShippoShipmentResponse response = shippoApiClient.createShipment(shipmentRequest);
                List<ShippoRate> rates = response.rates() != null ? response.rates() : List.of();

                Optional<ShippoRate> rate = rates.stream()
                    .filter(r -> r.provider().equals(order.getShippingCarrier())
                        && r.servicelevel().name().equals(order.getShippingService()))
                    .findFirst();

                if (rate.isEmpty()) {
                    return;
                }

                ShippoTransactionRequest transactionRequest = shippingMapper
                    .toTransactionRequest(rate.get().objectId());

                ShippoTransactionResponse transaction = shippoApiClient
                    .createTransaction(transactionRequest);

                if (!"SUCCESS".equals(transaction.status())) {
                    return;
                }

                OrderShipment shipment = orderShipmentMapper.toEntity(order, transaction);

                labels.add(new PurchasedLabel(shipment, packed.items()));
            }

            Instant labelCreatedAt = Instant.now();
            Map<Long, OrderItem> orderItemsBySkuId = order.getOrderItems().stream()
                .collect(Collectors.toMap(item ->
                    item.getProductSku().getId(), Function.identity())
                );

            for (PurchasedLabel label : labels) {
                OrderShipment shipment = label.shipment();

                orderShipmentRepository.save(shipment);

                ShipmentTrackingEvent trackingEvent = shipmentTrackingEventMapper.toEntity(
                    shipment, ShipmentStatus.PRE_TRANSIT, "Shipping label created",
                    originProperties.city(), labelCreatedAt
                );

                shipmentTrackingEventRepository.save(trackingEvent);

                List<OrderShipmentItem> shipmentItems = label.items()
                    .stream()
                    .collect(Collectors.groupingBy(
                        PackingItemResult::productSkuId, LinkedHashMap::new, Collectors.counting()
                    ))
                    .entrySet().stream()
                    .map(entry -> orderShipmentItemMapper.toEntity(
                        shipment, orderItemsBySkuId.get(entry.getKey()), entry.getValue().intValue()
                    ))
                    .toList();

                orderShipmentItemRepository.saveAll(shipmentItems);
            }
        } catch (ShippoApiException | UnpackableCartException _) {/**/}
    }

    private record PurchasedLabel(
        OrderShipment shipment,

        List<PackingItemResult> items
    ) {}

    private List<PackingItemResult> buildPackingItems(Order order) {
        List<PackingItemResult> packingItems = new ArrayList<>();

        for (OrderItem item : order.getOrderItems()) {
            PackingItemResult packingItem = shippingMapper.toPackingItem(item);

            for (int i = 0; i < item.getQuantity(); i++) {
                packingItems.add(packingItem);
            }
        }

        return packingItems;
    }
}
