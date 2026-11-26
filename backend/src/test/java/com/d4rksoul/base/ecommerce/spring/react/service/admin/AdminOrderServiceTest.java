package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderShipmentResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditEntityType;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidOrderTransitionException;
import com.d4rksoul.base.ecommerce.spring.react.exception.OrderNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminOrderMapper;
import com.d4rksoul.base.ecommerce.spring.react.pdf.ShippingSlipPdfGenerator;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderShipmentRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderService;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderShipmentService;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderStatusEventService;
import com.d4rksoul.base.ecommerce.spring.react.service.ShipmentTrackingEventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    private static final long ORDER_ID = 100L;
    private static final long SHIPMENT_ID = 5L;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderShipmentRepository orderShipmentRepository;
    @Mock
    private OrderService orderService;
    @Mock
    private OrderShipmentService orderShipmentService;
    @Mock
    private OrderStatusEventService orderStatusEventService;
    @Mock
    private ShipmentTrackingEventService shipmentTrackingEventService;
    @Mock
    private AdminAuditLogService adminAuditLogService;
    @Mock
    private AdminOrderMapper adminOrderMapper;
    @Mock
    private PageMapper pageMapper;
    @Mock
    private ShippingSlipPdfGenerator shippingSlipPdfGenerator;

    @Captor
    private ArgumentCaptor<OrderStatus> nextStatus;
    @Captor
    private ArgumentCaptor<Map<Long, List<ShipmentStatus>>> shipmentActions;

    @InjectMocks
    private AdminOrderService service;

    @Test
    @DisplayName("get order: rejects an order that does not exist or is not visible")
    void getOrderRejectsUnknownOrder() {
        when(orderRepository.findByIdAndStatusIn(ORDER_ID, OrderStatus.VISIBLE_STATUSES))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrder(ORDER_ID))
            .isInstanceOf(OrderNotFoundException.class);
    }

    @ParameterizedTest(name = "order {0} offers next status {1}")
    @CsvSource(value = {
        "PAID, PROCESSING",
        "PROCESSING, NULL",
        "SHIPPED, NULL",
        "DELIVERED, NULL"
    }, nullValues = "NULL")
    @DisplayName("get order: only a paid order can move to processing")
    void getOrderOffersNextStatusOnlyForPaidOrders(OrderStatus status, OrderStatus expectedNext) {
        Order order = givenVisibleOrder(status);
        OrderDetailsResponse details = OrderDetailsResponse.builder()
            .shipments(List.of())
            .build();

        when(orderService.buildDetails(order)).thenReturn(details);

        service.getOrder(ORDER_ID);

        verify(adminOrderMapper).toDetailsDto(eq(order), eq(details), nextStatus.capture(), any());

        assertThat(nextStatus.getValue()).isEqualTo(expectedNext);
    }

    @Test
    @DisplayName("get order: offers each parcel only the statuses it can move to")
    void getOrderOffersShipmentActions() {
        Order order = givenVisibleOrder(OrderStatus.PROCESSING);
        OrderDetailsResponse details = OrderDetailsResponse.builder()
            .shipments(List.of(
                OrderShipmentResponse.builder()
                    .id(1L)
                    .status(ShipmentStatus.PRE_TRANSIT)
                    .build(),

                OrderShipmentResponse.builder()
                    .id(2L)
                    .status(ShipmentStatus.OUT_FOR_DELIVERY)
                    .build(),

                OrderShipmentResponse.builder()
                    .id(3L)
                    .status(ShipmentStatus.DELIVERED)
                    .build()
            ))
            .build();

        when(orderService.buildDetails(order)).thenReturn(details);

        service.getOrder(ORDER_ID);

        verify(adminOrderMapper).toDetailsDto(eq(order), eq(details), any(), shipmentActions.capture());

        assertThat(shipmentActions.getValue()).containsEntry(1L, List.of(ShipmentStatus.IN_TRANSIT));
        assertThat(shipmentActions.getValue())
            .containsEntry(2L, List.of(ShipmentStatus.FAILURE, ShipmentStatus.DELIVERED));
        assertThat(shipmentActions.getValue()).containsEntry(3L, List.of());
    }

    @Test
    @DisplayName("shipping slip: rejects an order that does not exist")
    void shippingSlipRejectsUnknownOrder() {
        when(orderRepository.findByIdAndStatusIn(ORDER_ID, OrderStatus.HISTORY_STATUSES))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getShippingSlip(ORDER_ID))
            .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("shipping slip: generates the PDF with the parcels of the order")
    void shippingSlipUsesTheParcelsOfTheOrder() {
        Order order = Order.builder().id(ORDER_ID).build();
        List<OrderShipment> shipments = List.of(
            OrderShipment.builder()
                .id(SHIPMENT_ID)
                .build()
        );
        byte[] pdf = {1, 2, 3};

        when(orderRepository.findByIdAndStatusIn(ORDER_ID, OrderStatus.HISTORY_STATUSES))
            .thenReturn(Optional.of(order));
        when(orderShipmentRepository.findByOrderIdOrderByIdAsc(ORDER_ID)).thenReturn(shipments);
        when(shippingSlipPdfGenerator.generateShippingSlip(order, shipments)).thenReturn(pdf);

        assertThat(service.getShippingSlip(ORDER_ID)).isEqualTo(pdf);
    }

    @Test
    @DisplayName("change status: rejects an order that does not exist")
    void changeStatusRejectsUnknownOrder() {
        when(orderRepository.findByIdAndStatusIn(ORDER_ID, OrderStatus.VISIBLE_STATUSES))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeStatus(ORDER_ID, OrderStatus.PROCESSING))
            .isInstanceOf(OrderNotFoundException.class);
    }

    @ParameterizedTest(name = "{0} to {1} is not allowed")
    @CsvSource({
        "PAID, SHIPPED",
        "PAID, DELIVERED",
        "PAID, PAID",
        "PROCESSING, PROCESSING",
        "SHIPPED, PROCESSING",
        "DELIVERED, PROCESSING"
    })
    @DisplayName("change status: only paid to processing is allowed")
    void changeStatusRejectsAnyOtherTransition(OrderStatus current, OrderStatus target) {
        givenVisibleOrder(current);

        assertThatThrownBy(() -> service.changeStatus(ORDER_ID, target))
            .isInstanceOf(InvalidOrderTransitionException.class)
            .hasMessageContaining("cannot move from " + current + " to " + target);

        verify(orderStatusEventService, never()).transition(any(), any(), any());
        verify(orderShipmentService, never()).purchaseLabels(any());
    }

    @Test
    @DisplayName("change status: rejects when another request changed the order first, without buying labels")
    void changeStatusRejectsWhenOrderChangedInTheMeantime() {
        givenVisibleOrder(OrderStatus.PAID);

        when(orderStatusEventService.transition(ORDER_ID, OrderStatus.PAID, OrderStatus.PROCESSING))
            .thenReturn(false);

        assertThatThrownBy(() -> service.changeStatus(ORDER_ID, OrderStatus.PROCESSING))
            .isInstanceOf(InvalidOrderTransitionException.class)
            .hasMessageContaining("changed in the meantime");

        verify(orderShipmentService, never()).purchaseLabels(any());
    }

    @Test
    @DisplayName("change status: moves to processing, then buys the labels, then takes the order off automation")
    void changeStatusBuysLabelsAfterMovingToProcessing() {
        givenVisibleOrder(OrderStatus.PAID);

        when(orderStatusEventService.transition(ORDER_ID, OrderStatus.PAID, OrderStatus.PROCESSING))
            .thenReturn(true);
        when(orderShipmentRepository.existsByOrderId(ORDER_ID)).thenReturn(true);

        service.changeStatus(ORDER_ID, OrderStatus.PROCESSING);

        InOrder inOrder = inOrder(orderStatusEventService, orderShipmentService, orderRepository);
        inOrder.verify(orderStatusEventService).transition(ORDER_ID, OrderStatus.PAID, OrderStatus.PROCESSING);
        inOrder.verify(orderShipmentService).purchaseLabels(ORDER_ID);
        inOrder.verify(orderRepository).markManuallyManaged(ORDER_ID);
        verify(adminAuditLogService).register(
            AuditAction.STATUS_CHANGED,
            AuditEntityType.ORDER,
            ORDER_ID,
            "#100",
            "PAID → PROCESSING"
        );
    }

    @Test
    @DisplayName("change status: leaves the order on automation when no label could be bought")
    void changeStatusKeepsAutomationWhenNoLabelWasBought() {
        givenVisibleOrder(OrderStatus.PAID);

        when(orderStatusEventService.transition(ORDER_ID, OrderStatus.PAID, OrderStatus.PROCESSING))
            .thenReturn(true);
        when(orderShipmentRepository.existsByOrderId(ORDER_ID)).thenReturn(false);

        service.changeStatus(ORDER_ID, OrderStatus.PROCESSING);

        verify(orderRepository, never()).markManuallyManaged(any());
        verify(adminAuditLogService).register(
            AuditAction.STATUS_CHANGED,
            AuditEntityType.ORDER,
            ORDER_ID,
            "#100",
            "PAID → PROCESSING"
        );
    }

    @Test
    @DisplayName("change shipment status: rejects a parcel that does not belong to the order")
    void changeShipmentStatusRejectsUnknownParcel() {
        when(orderShipmentRepository.findByIdAndOrderId(SHIPMENT_ID, ORDER_ID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            service.changeShipmentStatus(ORDER_ID, SHIPMENT_ID, ShipmentStatus.IN_TRANSIT)
        ).isInstanceOf(OrderNotFoundException.class);
    }

    @ParameterizedTest(name = "parcel {0} to {1} is not allowed")
    @CsvSource({
        "PRE_TRANSIT, DELIVERED",
        "PRE_TRANSIT, OUT_FOR_DELIVERY",
        "IN_TRANSIT, DELIVERED",
        "DELIVERED, IN_TRANSIT",
        "FAILURE, IN_TRANSIT"
    })
    @DisplayName("change shipment status: rejects a jump that skips a step")
    void changeShipmentStatusRejectsInvalidTransition(ShipmentStatus current, ShipmentStatus target) {
        givenShipment(current);

        assertThatThrownBy(() -> service.changeShipmentStatus(ORDER_ID, SHIPMENT_ID, target))
            .isInstanceOf(InvalidOrderTransitionException.class)
            .hasMessageContaining("cannot move from " + current + " to " + target);

        verify(shipmentTrackingEventService, never()).transition(any(), any(), any());
        verify(adminAuditLogService, never()).register(any(), any(), any(), any(), any());
    }

    @ParameterizedTest(name = "parcel {0} to {1} is allowed")
    @CsvSource({
        "PRE_TRANSIT, IN_TRANSIT",
        "IN_TRANSIT, OUT_FOR_DELIVERY",
        "OUT_FOR_DELIVERY, FAILURE",
        "OUT_FOR_DELIVERY, DELIVERED",
        "FAILURE, OUT_FOR_DELIVERY",
        "FAILURE, DELIVERED"
    })
    @DisplayName("change shipment status: accepts each valid step, takes the order off automation and audits it")
    void changeShipmentStatusAcceptsValidTransition(ShipmentStatus current, ShipmentStatus target) {
        givenShipment(current);

        when(shipmentTrackingEventService.transition(SHIPMENT_ID, current, target)).thenReturn(true);

        service.changeShipmentStatus(ORDER_ID, SHIPMENT_ID, target);

        verify(orderRepository).markManuallyManaged(ORDER_ID);
        verify(adminAuditLogService).register(
            AuditAction.SHIPMENT_STATUS_CHANGED,
            AuditEntityType.ORDER,
            ORDER_ID,
            "#100",
            "Parcel 5: " + current + " → " + target
        );
    }

    @Test
    @DisplayName("change shipment status: rejects when another request changed the parcel first")
    void changeShipmentStatusRejectsWhenParcelChangedInTheMeantime() {
        givenShipment(ShipmentStatus.PRE_TRANSIT);

        when(shipmentTrackingEventService.transition(
            SHIPMENT_ID, ShipmentStatus.PRE_TRANSIT, ShipmentStatus.IN_TRANSIT
        )).thenReturn(false);

        assertThatThrownBy(() ->
            service.changeShipmentStatus(ORDER_ID, SHIPMENT_ID, ShipmentStatus.IN_TRANSIT)
        )
        .isInstanceOf(InvalidOrderTransitionException.class)
        .hasMessageContaining("changed in the meantime");

        verify(orderStatusEventService, never()).transition(any(), any(), any());
        verify(adminAuditLogService, never()).register(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("change shipment status: a parcel in transit moves the order to shipped")
    void parcelInTransitMovesOrderToShipped() {
        givenShipment(ShipmentStatus.PRE_TRANSIT);
        when(shipmentTrackingEventService.transition(
            SHIPMENT_ID, ShipmentStatus.PRE_TRANSIT, ShipmentStatus.IN_TRANSIT
        )).thenReturn(true);

        service.changeShipmentStatus(ORDER_ID, SHIPMENT_ID, ShipmentStatus.IN_TRANSIT);

        verify(orderStatusEventService).transition(ORDER_ID, OrderStatus.PROCESSING, OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("change shipment status: the order is not delivered while other parcels are still on the way")
    void orderStaysShippedWhileOtherParcelsAreNotDelivered() {
        givenShipment(ShipmentStatus.OUT_FOR_DELIVERY);

        when(shipmentTrackingEventService.transition(
            SHIPMENT_ID, ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.DELIVERED
        )).thenReturn(true);
        when(orderShipmentRepository.existsByOrderIdAndStatusNot(ORDER_ID, ShipmentStatus.DELIVERED))
            .thenReturn(true);

        service.changeShipmentStatus(ORDER_ID, SHIPMENT_ID, ShipmentStatus.DELIVERED);

        verify(orderStatusEventService, never())
            .transition(ORDER_ID, OrderStatus.SHIPPED, OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("change shipment status: the order is delivered when its last parcel is delivered")
    void orderIsDeliveredWhenLastParcelIsDelivered() {
        givenShipment(ShipmentStatus.OUT_FOR_DELIVERY);

        when(shipmentTrackingEventService.transition(
            SHIPMENT_ID, ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.DELIVERED
        )).thenReturn(true);
        when(orderShipmentRepository.existsByOrderIdAndStatusNot(ORDER_ID, ShipmentStatus.DELIVERED))
            .thenReturn(false);

        service.changeShipmentStatus(ORDER_ID, SHIPMENT_ID, ShipmentStatus.DELIVERED);

        verify(orderStatusEventService).transition(ORDER_ID, OrderStatus.SHIPPED, OrderStatus.DELIVERED);
    }

    private Order givenVisibleOrder(OrderStatus status) {
        Order order = Order.builder()
            .id(ORDER_ID)
            .status(status)
            .build();

        when(orderRepository.findByIdAndStatusIn(ORDER_ID, OrderStatus.VISIBLE_STATUSES))
            .thenReturn(Optional.of(order));

        return order;
    }

    private void givenShipment(ShipmentStatus status) {
        OrderShipment shipment = OrderShipment.builder()
            .id(SHIPMENT_ID)
            .status(status)
            .build();

        when(orderShipmentRepository.findByIdAndOrderId(SHIPMENT_ID, ORDER_ID))
            .thenReturn(Optional.of(shipment));
    }
}
