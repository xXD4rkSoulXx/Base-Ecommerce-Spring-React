package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ReturnedQuantityProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeRefundResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturnItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditEntityType;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.RefundFailedException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ReturnNotAllowedException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ReturnNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.StripeApiException;
import com.d4rksoul.base.ecommerce.spring.react.integration.stripe.StripeApiClient;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminReturnMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderStatusEventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReturnServiceTest {

    private static final long RETURN_ID = 7L;
    private static final long ORDER_ID = 100L;

    @Mock
    private OrderReturnRepository orderReturnRepository;
    @Mock
    private OrderReturnItemRepository orderReturnItemRepository;
    @Mock
    private ProductSkuRepository productSkuRepository;
    @Mock
    private OrderStatusEventService orderStatusEventService;
    @Mock
    private AdminAuditLogService adminAuditLogService;
    @Mock
    private AdminReturnMapper adminReturnMapper;
    @Mock
    private PageMapper pageMapper;
    @Mock
    private StripeApiClient stripeApiClient;

    @Captor
    private ArgumentCaptor<BigDecimal> refundedAmount;

    @InjectMocks
    private AdminReturnService service;

    private final OrderItem itemA = orderItem(1L, 3L, "50.00", 2);
    private final OrderItem itemB = orderItem(2L, 4L, "20.00", 1);

    @Test
    @DisplayName("approve: approves a requested return and registers it in the audit log")
    void approveApprovesRequestedReturn() {
        OrderReturn orderReturn = givenReturn(
            ReturnStatus.REQUESTED, order(0, "125.00")
        );

        service.approve(RETURN_ID);

        assertThat(orderReturn.getStatus()).isEqualTo(ReturnStatus.APPROVED);
        assertThat(orderReturn.getApprovedAt()).isNotNull();

        verify(orderReturnRepository).markManuallyManaged(RETURN_ID);
        verify(adminAuditLogService).register(
            AuditAction.APPROVED, AuditEntityType.RETURN, RETURN_ID, "#7", null
        );
    }

    @Test
    @DisplayName("approve: rejects a return that is not in the requested status")
    void approveRejectsReturnInAnotherStatus() {
        givenNoReturnInStatus(ReturnStatus.REQUESTED, true);

        assertThatThrownBy(() -> service.approve(RETURN_ID))
            .isInstanceOf(ReturnNotAllowedException.class);
    }

    @Test
    @DisplayName("approve: reports a return that does not exist")
    void approveReportsUnknownReturn() {
        givenNoReturnInStatus(ReturnStatus.REQUESTED, false);

        assertThatThrownBy(() -> service.approve(RETURN_ID))
            .isInstanceOf(ReturnNotFoundException.class);
    }

    @Test
    @DisplayName("reject: rejects the return with a trimmed reason and sends the order back to delivered")
    void rejectRejectsReturnAndRestoresOrder() {
        OrderReturn orderReturn = givenReturn(ReturnStatus.REQUESTED, order(0, "125.00"));

        service.reject(RETURN_ID, "  Damaged by customer  ");

        assertThat(orderReturn.getStatus()).isEqualTo(ReturnStatus.REJECTED);
        assertThat(orderReturn.getRejectionReason()).isEqualTo("Damaged by customer");
        assertThat(orderReturn.getRejectedAt()).isNotNull();

        verify(orderStatusEventService).transition(
            ORDER_ID, OrderStatus.RETURN_REQUESTED, OrderStatus.DELIVERED
        );
        verify(adminAuditLogService).register(
            AuditAction.REJECTED,
            AuditEntityType.RETURN,
            RETURN_ID,
            "#7",
            "Damaged by customer"
        );
    }

    @Test
    @DisplayName("receive: marks the return as received and gives the stock back")
    void receiveGivesStockBack() {
        OrderReturn orderReturn = givenReturn(
            ReturnStatus.APPROVED, order(0, "125.00")
        );

        when(orderReturnItemRepository.findByOrderReturnId(RETURN_ID))
            .thenReturn(List.of(returnItem(itemA, 2), returnItem(itemB, 1)));

        service.receive(RETURN_ID);

        assertThat(orderReturn.getStatus()).isEqualTo(ReturnStatus.RECEIVED);
        assertThat(orderReturn.getReceivedAt()).isNotNull();

        verify(productSkuRepository).incrementStock(3L, 2);
        verify(productSkuRepository).incrementStock(4L, 1);
    }

    @Test
    @DisplayName("receive: rejects a return that was not approved and does not touch the stock")
    void receiveRejectsReturnNotApproved() {
        givenNoReturnInStatus(ReturnStatus.APPROVED, true);

        assertThatThrownBy(() -> service.receive(RETURN_ID))
            .isInstanceOf(ReturnNotAllowedException.class);

        verify(productSkuRepository, never()).incrementStock(any(), anyInt());
    }

    @Test
    @DisplayName("refund: partial return refunds the share of the items and sends the order back to delivered")
    void refundPartialReturn() {
        Order order = order(0, "125.00");
        OrderReturn orderReturn = givenReturn(ReturnStatus.RECEIVED, order);

        givenRefundData(
            List.of(returnItem(itemA, 1)),
            List.of(new ReturnedQuantityProjection(1L, 1L)),
            "0.00"
        );
        when(stripeApiClient.createRefund(any(Order.class), any(OrderReturn.class), any(BigDecimal.class)))
            .thenReturn(new StripeRefundResult("re_1", "succeeded"));

        service.refund(RETURN_ID);

        assertThat(orderReturn.getStatus()).isEqualTo(ReturnStatus.REFUNDED);
        assertThat(orderReturn.getRefundAmount()).isEqualByComparingTo("51.67");
        assertThat(orderReturn.getStripeRefundId()).isEqualTo("re_1");
        assertThat(orderReturn.getRefundedAt()).isNotNull();

        verify(orderStatusEventService).transition(
            ORDER_ID, OrderStatus.RETURN_REQUESTED, OrderStatus.DELIVERED
        );
        verify(adminAuditLogService).register(
            AuditAction.REFUNDED, AuditEntityType.RETURN, RETURN_ID, "#7", null
        );
    }

    @Test
    @DisplayName("refund: when everything was returned, refunds the remaining balance and marks the order refunded")
    void refundFullyReturnedOrderRefundsRemainingBalance() {
        Order order = order(0, "125.00");
        givenReturn(ReturnStatus.RECEIVED, order);
        givenRefundData(
            List.of(returnItem(itemB, 1)),
            List.of(
                new ReturnedQuantityProjection(1L, 2L),
                new ReturnedQuantityProjection(2L, 1L)
            ),
            "51.67"
        );
        when(stripeApiClient.createRefund(
            any(Order.class),
            any(OrderReturn.class),
            any(BigDecimal.class)
        )).thenReturn(new StripeRefundResult("re_2", "succeeded"));

        service.refund(RETURN_ID);

        verify(stripeApiClient)
            .createRefund(any(Order.class), any(OrderReturn.class), refundedAmount.capture());

        assertThat(refundedAmount.getValue()).isEqualByComparingTo("73.33");

        verify(orderStatusEventService)
            .transition(ORDER_ID, OrderStatus.RETURN_REQUESTED, OrderStatus.REFUNDED);
    }

    @Test
    @DisplayName("refund: reports a failure when Stripe throws and keeps the return as received")
    void refundReportsStripeError() {
        OrderReturn orderReturn = givenReturn(
            ReturnStatus.RECEIVED, order(0, "125.00")
        );

        givenRefundData(
            List.of(returnItem(itemA, 1)),
            List.of(new ReturnedQuantityProjection(1L, 1L)),
            "0.00"
        );

        when(stripeApiClient.createRefund(
            any(Order.class), any(OrderReturn.class), any(BigDecimal.class))
        ).thenThrow(new StripeApiException("Stripe down", new RuntimeException()));

        assertThatThrownBy(() -> service.refund(RETURN_ID))
            .isInstanceOf(RefundFailedException.class);
        assertThat(orderReturn.getStatus()).isEqualTo(ReturnStatus.RECEIVED);

        verify(orderStatusEventService, never()).transition(any(), any(), any());
    }

    @ParameterizedTest(name = "Stripe status {0} is a failed refund")
    @ValueSource(strings = {"failed", "canceled"})
    @DisplayName("refund: reports a failure when Stripe marks the refund as failed or canceled")
    void refundReportsFailedStatus(String stripeStatus) {
        OrderReturn orderReturn = givenReturn(
            ReturnStatus.RECEIVED, order(0, "125.00")
        );

        givenRefundData(
            List.of(returnItem(itemA, 1)),
            List.of(new ReturnedQuantityProjection(1L, 1L)),
            "0.00"
        );
        when(stripeApiClient.createRefund(
            any(Order.class), any(OrderReturn.class), any(BigDecimal.class))
        ).thenReturn(new StripeRefundResult("re_1", stripeStatus));

        assertThatThrownBy(() -> service.refund(RETURN_ID))
            .isInstanceOf(RefundFailedException.class);
        assertThat(orderReturn.getStatus()).isEqualTo(ReturnStatus.RECEIVED);

        verify(orderStatusEventService, never()).transition(any(), any(), any());
    }

    @Test
    @DisplayName("refund: rejects a return that was not received yet")
    void refundRejectsReturnNotReceived() {
        givenNoReturnInStatus(ReturnStatus.RECEIVED, true);

        assertThatThrownBy(() -> service.refund(RETURN_ID))
            .isInstanceOf(ReturnNotAllowedException.class);

        verify(stripeApiClient, never())
            .createRefund(any(Order.class), any(OrderReturn.class), any(BigDecimal.class));
    }

    @Test
    @DisplayName("calculate refund: the last return gets the whole remaining balance")
    void calculateRefundFullyReturnedGetsRemaining() {
        Order order = order(0, "125.00");

        BigDecimal amount = service.calculateRefund(
            order, List.of(returnItem(itemB, 1)), new BigDecimal("73.33"), true
        );

        assertThat(amount).isEqualByComparingTo("73.33");
    }

    @Test
    @DisplayName("calculate refund: partial return = returned items + shipping share by weight")
    void calculateRefundPartialReturn() {
        Order order = order(0, "125.00");

        BigDecimal amount = service.calculateRefund(
            order,
            List.of(returnItem(itemA, 1)),
            new BigDecimal("125.00"),
            false
        );

        assertThat(amount).isEqualByComparingTo("51.67");
    }

    @Test
    @DisplayName("calculate refund: applies the order discount to the returned items")
    void calculateRefundAppliesOrderDiscount() {
        Order order = order(20, "101.00");

        BigDecimal amount = service.calculateRefund(
            order,
            List.of(returnItem(itemA, 1)),
            new BigDecimal("101.00"),
            false
        );

        assertThat(amount).isEqualByComparingTo("41.67");
    }

    @Test
    @DisplayName("calculate refund: never refunds more than the remaining balance")
    void calculateRefundIsCappedByRemainingBalance() {
        Order order = order(0, "125.00");

        BigDecimal amount = service.calculateRefund(
            order,
            List.of(returnItem(itemA, 1)),
            new BigDecimal("30.00"),
            false
        );

        assertThat(amount).isEqualByComparingTo("30.00");
    }

    @Test
    @DisplayName("calculate refund: refunds zero for a free order with free shipping")
    void calculateRefundIsZeroForFreeOrder() {
        OrderItem freeItem = orderItem(9L, 9L, "0.00", 1);
        Order order = order(0, "0.00", "0.00", freeItem);

        BigDecimal amount = service.calculateRefund(
            order, List.of(returnItem(freeItem, 1)), BigDecimal.ZERO, false
        );

        assertThat(amount).isEqualByComparingTo("0.00");
    }

    private OrderReturn givenReturn(ReturnStatus status, Order order) {
        OrderReturn orderReturn = OrderReturn.builder()
            .id(RETURN_ID)
            .status(status)
            .order(order)
            .build();

        when(orderReturnRepository.findByIdAndStatus(RETURN_ID, status))
            .thenReturn(Optional.of(orderReturn));

        return orderReturn;
    }

    private void givenNoReturnInStatus(ReturnStatus status, boolean returnExists) {
        when(orderReturnRepository.findByIdAndStatus(RETURN_ID, status))
            .thenReturn(Optional.empty());
        when(orderReturnRepository.existsById(RETURN_ID)).thenReturn(returnExists);
    }

    private void givenRefundData(
        List<OrderReturnItem> returnItems,
        List<ReturnedQuantityProjection> returned,
        String alreadyRefunded
    ) {
        when(orderReturnItemRepository.findByOrderReturnId(RETURN_ID)).thenReturn(returnItems);
        when(orderReturnItemRepository.sumReturnedQuantities(
            ORDER_ID, ReturnStatus.INACTIVE_STATUSES
        )).thenReturn(returned);
        when(orderReturnRepository.sumRefundAmountByOrderIdAndStatus(
            ORDER_ID, ReturnStatus.REFUNDED
        )).thenReturn(new BigDecimal(alreadyRefunded));
    }

    private Order order(int discountPercentage, String total) {
        return order(discountPercentage, "5.00", total, itemA, itemB);
    }

    private static Order order(
        int discountPercentage, String shippingCost, String total, OrderItem... items
    ) {
        return Order.builder()
            .id(ORDER_ID)
            .discountPercentage(discountPercentage)
            .shippingCost(new BigDecimal(shippingCost))
            .totalAmount(new BigDecimal(total))
            .orderItems(new ArrayList<>(List.of(items)))
            .build();
    }

    private static OrderItem orderItem(long id, long skuId, String price, int quantity) {
        return OrderItem.builder()
            .id(id)
            .productSku(ProductSku.builder().id(skuId).build())
            .price(new BigDecimal(price))
            .discountPercentage(0)
            .quantity(quantity)
            .weightGrams(500)
            .build();
    }

    private static OrderReturnItem returnItem(OrderItem orderItem, int quantity) {
        return OrderReturnItem.builder().orderItem(orderItem).quantity(quantity).build();
    }
}
