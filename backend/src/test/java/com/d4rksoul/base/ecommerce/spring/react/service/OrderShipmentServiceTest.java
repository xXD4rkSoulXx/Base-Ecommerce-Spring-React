package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.config.ShippingOriginProperties;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackedParcelItemsResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackedParcelResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PackingItemResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.ShippoApiException;
import com.d4rksoul.base.ecommerce.spring.react.exception.UnpackableCartException;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.ShippoApiClient;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoRate;
import com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto.ShippoServiceLevel;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderShipmentServiceTest {

    private static final long ORDER_ID = 100L;
    private static final long SKU_A = 10L;
    private static final long SKU_B = 11L;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderShipmentRepository orderShipmentRepository;
    @Mock
    private OrderShipmentItemRepository orderShipmentItemRepository;
    @Mock
    private ShipmentTrackingEventRepository shipmentTrackingEventRepository;
    @Mock
    private ParcelPackingService parcelPackingService;
    @Mock
    private OrderShipmentMapper orderShipmentMapper;
    @Mock
    private OrderShipmentItemMapper orderShipmentItemMapper;
    @Mock
    private ShipmentTrackingEventMapper shipmentTrackingEventMapper;
    @Mock
    private ShippoApiClient shippoApiClient;
    @Mock
    private ShippingOriginProperties originProperties;
    @Spy
    private ShippingMapper shippingMapper = new ShippingMapper();

    @Captor
    private ArgumentCaptor<List<PackingItemResult>> packedItems;
    @Captor
    private ArgumentCaptor<ShippoTransactionRequest> transactionRequest;

    @InjectMocks
    private OrderShipmentService service;

    private final OrderItem itemA = orderItem(1L, SKU_A, 2);
    private final OrderItem itemB = orderItem(2L, SKU_B, 1);
    private final Order order = Order.builder()
        .id(ORDER_ID)
        .shippingCarrier("CTT")
        .shippingService("Standard")
        .orderItems(new ArrayList<>(List.of(itemA, itemB)))
        .build();

    @Test
    @DisplayName("does nothing when the order does not exist")
    void doesNothingWhenOrderDoesNotExist() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        service.purchaseLabels(ORDER_ID);

        verifyNoInteractions(parcelPackingService, shippoApiClient, orderShipmentRepository);
    }

    @Test
    @DisplayName("packs one entry per unit of each order item")
    void packsOneEntryPerUnit() {
        givenOrder();
        givenParcels(packed(SKU_A, SKU_A, SKU_B));
        givenShippoOffers(rate("CTT", "Standard", "rate_ctt"));

        when(shippoApiClient.createTransaction(any(ShippoTransactionRequest.class)))
            .thenReturn(transaction("SUCCESS"));

        service.purchaseLabels(ORDER_ID);

        verify(parcelPackingService).pack(packedItems.capture());

        assertThat(packedItems.getValue()).hasSize(3);
        assertThat(packedItems.getValue())
            .extracting(PackingItemResult::productSkuId)
            .containsExactlyInAnyOrder(SKU_A, SKU_A, SKU_B);
    }

    @Test
    @DisplayName("buys the label of the rate that matches the carrier and service of the order")
    void buysTheLabelOfTheMatchingRate() {
        givenOrder();
        givenParcels(packed(SKU_A, SKU_A));
        givenShippoOffers(
            rate("DHL", "Express", "rate_dhl"),
            rate("CTT", "Standard", "rate_ctt")
        );

        when(shippoApiClient.createTransaction(any(ShippoTransactionRequest.class)))
            .thenReturn(transaction("SUCCESS"));

        service.purchaseLabels(ORDER_ID);

        verify(shippoApiClient).createTransaction(transactionRequest.capture());

        assertThat(transactionRequest.getValue().rate()).isEqualTo("rate_ctt");
        assertThat(transactionRequest.getValue().async()).isFalse();
    }

    @Test
    @DisplayName("saves the shipment, a pre-transit tracking event and one item row per product")
    void savesShipmentTrackingEventAndGroupedItems() {
        ShippoTransactionResponse transaction = transaction("SUCCESS");

        OrderShipment shipment = OrderShipment.builder()
            .id(1L)
            .build();

        givenOrder();
        givenParcels(packed(SKU_A, SKU_A));
        givenShippoOffers(rate("CTT", "Standard", "rate_ctt"));

        when(shippoApiClient.createTransaction(any(ShippoTransactionRequest.class)))
            .thenReturn(transaction);
        when(orderShipmentMapper.toEntity(order, transaction)).thenReturn(shipment);

        service.purchaseLabels(ORDER_ID);

        verify(orderShipmentRepository).save(shipment);
        verify(shipmentTrackingEventMapper).toEntity(
            eq(shipment), eq(ShipmentStatus.PRE_TRANSIT),
            eq("Shipping label created"), any(), any(Instant.class)
        );
        verify(shipmentTrackingEventRepository).save(any());
        verify(orderShipmentItemMapper).toEntity(shipment, itemA, 2);
        verify(orderShipmentItemRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("saves one shipment per parcel, each with its own items")
    void savesOneShipmentPerParcel() {
        ShippoTransactionResponse firstTransaction = new ShippoTransactionResponse(
            "tx_1", "SUCCESS", "TRACK1"
        );
        ShippoTransactionResponse secondTransaction = new ShippoTransactionResponse(
            "tx_2", "SUCCESS", "TRACK2"
        );

        OrderShipment firstShipment = OrderShipment.builder()
            .id(1L)
            .build();

        OrderShipment secondShipment = OrderShipment.builder()
            .id(2L)
            .build();

        givenOrder();
        givenParcels(packed(SKU_A, SKU_A), packed(SKU_B));
        givenShippoOffers(rate("CTT", "Standard", "rate_ctt"));

        when(shippoApiClient.createTransaction(any(ShippoTransactionRequest.class)))
            .thenReturn(firstTransaction, secondTransaction);
        when(orderShipmentMapper.toEntity(order, firstTransaction)).thenReturn(firstShipment);
        when(orderShipmentMapper.toEntity(order, secondTransaction)).thenReturn(secondShipment);

        service.purchaseLabels(ORDER_ID);

        verify(orderShipmentRepository).save(firstShipment);
        verify(orderShipmentRepository).save(secondShipment);
        verify(orderShipmentItemMapper).toEntity(firstShipment, itemA, 2);
        verify(orderShipmentItemMapper).toEntity(secondShipment, itemB, 1);
    }

    @Test
    @DisplayName("buys and saves nothing when the carrier service of the order is no longer offered")
    void doesNothingWhenRateIsNotOffered() {
        givenOrder();
        givenParcels(packed(SKU_A, SKU_A));
        givenShippoOffers(rate("DHL", "Express", "rate_dhl"));

        service.purchaseLabels(ORDER_ID);

        verify(shippoApiClient, never()).createTransaction(any(ShippoTransactionRequest.class));
        verifyNoInteractions(
            orderShipmentRepository, shipmentTrackingEventRepository, orderShipmentItemRepository
        );
    }

    @Test
    @DisplayName("saves nothing when the label purchase is not successful")
    void savesNothingWhenPurchaseFails() {
        givenOrder();
        givenParcels(packed(SKU_A, SKU_A));
        givenShippoOffers(rate("CTT", "Standard", "rate_ctt"));

        when(shippoApiClient.createTransaction(any(ShippoTransactionRequest.class)))
            .thenReturn(transaction("ERROR"));

        service.purchaseLabels(ORDER_ID);

        verifyNoInteractions(
            orderShipmentRepository, shipmentTrackingEventRepository, orderShipmentItemRepository
        );
    }

    @Test
    @DisplayName("saves nothing when a later parcel fails, even though the first label was already bought")
    void savesNothingWhenLaterParcelFails() {
        ShippoTransactionResponse firstTransaction = transaction("SUCCESS");

        givenOrder();
        givenParcels(packed(SKU_A, SKU_A), packed(SKU_B));
        givenShippoOffers(rate("CTT", "Standard", "rate_ctt"));

        when(shippoApiClient.createTransaction(any(ShippoTransactionRequest.class)))
            .thenReturn(firstTransaction, transaction("ERROR"));
        when(orderShipmentMapper.toEntity(order, firstTransaction))
            .thenReturn(OrderShipment.builder().build());

        service.purchaseLabels(ORDER_ID);

        verify(shippoApiClient, times(2))
            .createTransaction(any(ShippoTransactionRequest.class));
        verifyNoInteractions(
            orderShipmentRepository, shipmentTrackingEventRepository, orderShipmentItemRepository
        );
    }

    @Test
    @DisplayName("does not fail when Shippo is down, and saves nothing")
    void swallowsShippoErrors() {
        givenOrder();
        givenParcels(packed(SKU_A, SKU_A));

        when(shippoApiClient.createShipment(any(ShippoShipmentRequest.class)))
            .thenThrow(new ShippoApiException("Shippo down", new RuntimeException()));

        assertThatCode(() -> service.purchaseLabels(ORDER_ID)).doesNotThrowAnyException();

        verifyNoInteractions(orderShipmentRepository);
    }

    @Test
    @DisplayName("does not fail when the order cannot be packed, and saves nothing")
    void swallowsUnpackableOrder() {
        givenOrder();

        when(parcelPackingService.pack(anyList())).thenThrow(new UnpackableCartException("Too big"));

        assertThatCode(() -> service.purchaseLabels(ORDER_ID)).doesNotThrowAnyException();

        verifyNoInteractions(shippoApiClient, orderShipmentRepository);
    }

    private void givenOrder() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
    }

    private void givenParcels(PackedParcelItemsResult... parcels) {
        when(parcelPackingService.pack(anyList())).thenReturn(Arrays.asList(parcels));
    }

    private void givenShippoOffers(ShippoRate... rates) {
        when(shippoApiClient.createShipment(any(ShippoShipmentRequest.class)))
            .thenReturn(new ShippoShipmentResponse("shp_1", List.of(rates)));
    }

    private static PackedParcelItemsResult packed(long... skuIds) {
        List<PackingItemResult> items = Arrays.stream(skuIds)
            .mapToObj(skuId ->
                PackingItemResult.builder()
                    .productSkuId(skuId)
                    .weightKg(0.5)
                    .build()
            )
            .toList();

        PackedParcelResult parcel = PackedParcelResult.builder()
            .boxName("Box")
            .weightKg(1)
            .lengthCm(20)
            .widthCm(15)
            .heightCm(10)
            .build();

        return PackedParcelItemsResult.builder().parcel(parcel).items(items).build();
    }

    private static ShippoRate rate(String provider, String service, String objectId) {
        return new ShippoRate(
            objectId,
            "5.00",
            "EUR",
            provider,
            new ShippoServiceLevel(service, "token"),
            2
        );
    }

    private static ShippoTransactionResponse transaction(String status) {
        return new ShippoTransactionResponse("tx_" + status, status, "TRACK1");
    }

    private static OrderItem orderItem(long id, long skuId, int quantity) {
        return OrderItem.builder()
            .id(id)
            .productSku(ProductSku.builder().id(skuId).build())
            .quantity(quantity)
            .weightGrams(500)
            .lengthCm(10)
            .widthCm(10)
            .heightCm(5)
            .build();
    }
}
