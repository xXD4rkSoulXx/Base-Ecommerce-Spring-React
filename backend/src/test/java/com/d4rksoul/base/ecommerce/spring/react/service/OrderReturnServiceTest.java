package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ReturnedQuantityProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.CreateReturnItemRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.CreateReturnRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderReturnResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturnItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderStatusEvent;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnReason;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidReturnRequestException;
import com.d4rksoul.base.ecommerce.spring.react.exception.OrderNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ReturnNotAllowedException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ReturnNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderReturnItemMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderReturnMapper;
import com.d4rksoul.base.ecommerce.spring.react.pdf.ShippingSlipPdfGenerator;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderStatusEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderReturnServiceTest {

    private static final long ORDER_ID = 100L;
    private static final long ITEM_ID = 10L;
    private static final long RETURN_ID = 7L;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderReturnRepository orderReturnRepository;
    @Mock
    private OrderReturnItemRepository orderReturnItemRepository;
    @Mock
    private OrderStatusEventRepository orderStatusEventRepository;
    @Mock
    private UserService userService;
    @Mock
    private OrderStatusEventService orderStatusEventService;
    @Mock
    private OrderReturnMapper orderReturnMapper;
    @Mock
    private OrderReturnItemMapper orderReturnItemMapper;
    @Mock
    private ShippingSlipPdfGenerator shippingSlipPdfGenerator;

    @InjectMocks
    private OrderReturnService service;

    private final User user = User.builder()
        .id(1L)
        .build();

    private final OrderItem orderItem = OrderItem.builder()
        .id(ITEM_ID)
        .quantity(3)
        .productName("Jacket")
        .build();

    private final Order order = Order.builder()
        .id(ORDER_ID)
        .orderItems(List.of(orderItem))
        .build();

    @Test
    @DisplayName("rejects reason OTHER without a comment")
    void rejectsOtherReasonWithoutComment() {
        CreateReturnRequest request = request(
            ReturnReason.OTHER, "   ", new CreateReturnItemRequest(ITEM_ID, 1)
        );

        assertThatThrownBy(() -> service.requestReturn(ORDER_ID, request))
            .isInstanceOf(InvalidReturnRequestException.class)
            .hasMessageContaining("describe the reason");

        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("rejects an order that is not delivered or does not belong to the user")
    void rejectsOrderNotDelivered() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByIdAndUserIdAndStatusIn(ORDER_ID, 1L, List.of(OrderStatus.DELIVERED)))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requestReturn(ORDER_ID, validRequest(1)))
            .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("rejects an order without a delivery date")
    void rejectsOrderWithoutDeliveryDate() {
        givenDeliveredOrder(null);

        assertThatThrownBy(() -> service.requestReturn(ORDER_ID, validRequest(1)))
            .isInstanceOf(ReturnNotAllowedException.class)
            .hasMessageContaining("delivery date");
    }

    @Test
    @DisplayName("rejects a return after the return window has expired")
    void rejectsExpiredReturnWindow() {
        givenDeliveredOrder(Instant.now().minus(Constants.RETURN_WINDOW).minus(Duration.ofDays(1)));

        assertThatThrownBy(() -> service.requestReturn(ORDER_ID, validRequest(1)))
            .isInstanceOf(ReturnNotAllowedException.class)
            .hasMessageContaining("expired");

        verify(orderStatusEventService, never()).transition(any(), any(), any());
    }

    @Test
    @DisplayName("rejects an item that does not belong to the order")
    void rejectsItemFromAnotherOrder() {
        givenDeliveredOrder(Instant.now().minus(Duration.ofDays(1)));

        CreateReturnRequest request = request(
            ReturnReason.DEFECTIVE, null, new CreateReturnItemRequest(999L, 1)
        );

        assertThatThrownBy(() -> service.requestReturn(ORDER_ID, request))
            .isInstanceOf(InvalidReturnRequestException.class)
            .hasMessageContaining("does not belong");
    }

    @Test
    @DisplayName("rejects the same item twice in one return")
    void rejectsDuplicateItem() {
        givenDeliveredOrder(Instant.now().minus(Duration.ofDays(1)));

        CreateReturnRequest request = request(
            ReturnReason.DEFECTIVE,
            null,
            new CreateReturnItemRequest(ITEM_ID, 1),
            new CreateReturnItemRequest(ITEM_ID, 1)
        );

        assertThatThrownBy(() -> service.requestReturn(ORDER_ID, request))
            .isInstanceOf(InvalidReturnRequestException.class)
            .hasMessageContaining("only appear once");
    }

    @Test
    @DisplayName("rejects a quantity above what is left to return after earlier returns")
    void rejectsQuantityAboveWhatIsLeft() {
        givenDeliveredOrder(Instant.now().minus(Duration.ofDays(1)));

        when(orderReturnItemRepository.sumReturnedQuantities(ORDER_ID, ReturnStatus.INACTIVE_STATUSES))
            .thenReturn(List.of(new ReturnedQuantityProjection(ITEM_ID, 2L)));

        assertThatThrownBy(() -> service.requestReturn(ORDER_ID, validRequest(2)))
            .isInstanceOf(InvalidReturnRequestException.class)
            .hasMessageContaining("only 1 left to return");
    }

    @Test
    @DisplayName("rejects a return when the order status changed in the meantime")
    void rejectsWhenTransitionFails() {
        givenDeliveredOrder(Instant.now().minus(Duration.ofDays(1)));

        when(orderStatusEventService.transition(ORDER_ID, OrderStatus.DELIVERED, OrderStatus.RETURN_REQUESTED))
            .thenReturn(false);

        assertThatThrownBy(() -> service.requestReturn(ORDER_ID, validRequest(1)))
            .isInstanceOf(ReturnNotAllowedException.class)
            .hasMessageContaining("no longer eligible");

        verify(orderReturnRepository, never()).save(any(OrderReturn.class));
    }

    @Test
    @DisplayName("creates the return inside the window, with a trimmed comment")
    void createsReturnInsideWindow() {
        givenDeliveredOrder(Instant.now().minus(Constants.RETURN_WINDOW).plus(Duration.ofDays(1)));

        when(orderStatusEventService.transition(ORDER_ID, OrderStatus.DELIVERED, OrderStatus.RETURN_REQUESTED))
            .thenReturn(true);

        OrderReturn orderReturn = OrderReturn.builder().build();
        OrderReturnItem returnItem = OrderReturnItem.builder().build();
        OrderReturnResponse response = OrderReturnResponse.builder().build();

        when(orderReturnMapper.toEntity(order, ReturnReason.DOES_NOT_FIT, "Too small"))
            .thenReturn(orderReturn);
        when(orderReturnItemMapper.toEntity(orderReturn, orderItem, 2)).thenReturn(returnItem);
        when(orderReturnItemRepository.saveAll(List.of(returnItem))).thenReturn(List.of(returnItem));
        when(orderReturnMapper.toDto(orderReturn, List.of(returnItem))).thenReturn(response);

        CreateReturnRequest request = request(
            ReturnReason.DOES_NOT_FIT,
            "  Too small  ",
            new CreateReturnItemRequest(ITEM_ID, 2)
        );

        OrderReturnResponse result = service.requestReturn(ORDER_ID, request);

        assertThat(result).isSameAs(response);

        verify(orderReturnRepository).save(orderReturn);
    }

    @Test
    @DisplayName("allows returning the remaining quantity of a partially returned item")
    void allowsReturningTheRemainingQuantity() {
        givenDeliveredOrder(Instant.now().minus(Duration.ofDays(1)));

        when(orderReturnItemRepository.sumReturnedQuantities(ORDER_ID, ReturnStatus.INACTIVE_STATUSES))
            .thenReturn(List.of(new ReturnedQuantityProjection(ITEM_ID, 2L)));
        when(orderStatusEventService.transition(ORDER_ID, OrderStatus.DELIVERED, OrderStatus.RETURN_REQUESTED))
            .thenReturn(true);

        OrderReturn orderReturn = OrderReturn.builder().build();
        OrderReturnItem returnItem = OrderReturnItem.builder().build();

        when(orderReturnMapper.toEntity(order, ReturnReason.DEFECTIVE, null)).thenReturn(orderReturn);
        when(orderReturnItemMapper.toEntity(orderReturn, orderItem, 1)).thenReturn(returnItem);
        when(orderReturnItemRepository.saveAll(List.of(returnItem))).thenReturn(List.of(returnItem));

        service.requestReturn(ORDER_ID, validRequest(1));

        verify(orderReturnRepository).save(orderReturn);
    }

    @Test
    @DisplayName("rejects a return slip for a return that does not exist")
    void returnSlipRejectsUnknownReturn() {
        givenOrderInHistory();

        when(orderReturnRepository.findByIdAndOrderId(RETURN_ID, ORDER_ID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getReturnSlip(ORDER_ID, RETURN_ID))
            .isInstanceOf(ReturnNotFoundException.class);
    }

    @Test
    @DisplayName("rejects a return slip until the return is approved")
    void returnSlipRejectsReturnNotApproved() {
        givenOrderInHistory();

        OrderReturn orderReturn = OrderReturn.builder()
            .status(ReturnStatus.REQUESTED)
            .build();

        when(orderReturnRepository.findByIdAndOrderId(RETURN_ID, ORDER_ID))
            .thenReturn(Optional.of(orderReturn));

        assertThatThrownBy(() -> service.getReturnSlip(ORDER_ID, RETURN_ID))
            .isInstanceOf(ReturnNotAllowedException.class)
            .hasMessageContaining("approved");

        verifyNoInteractions(shippingSlipPdfGenerator);
    }

    @Test
    @DisplayName("generates the return slip for an approved return")
    void returnSlipForApprovedReturn() {
        givenOrderInHistory();

        OrderReturn orderReturn = OrderReturn.builder()
            .status(ReturnStatus.APPROVED)
            .build();

        List<OrderReturnItem> returnItems = List.of(OrderReturnItem.builder().build());
        byte[] pdf = {1, 2, 3};

        when(orderReturnRepository.findByIdAndOrderId(RETURN_ID, ORDER_ID))
            .thenReturn(Optional.of(orderReturn));
        when(orderReturnItemRepository.findByOrderReturnId(RETURN_ID)).thenReturn(returnItems);
        when(shippingSlipPdfGenerator.generateReturnSlip(orderReturn, returnItems)).thenReturn(pdf);

        assertThat(service.getReturnSlip(ORDER_ID, RETURN_ID)).isEqualTo(pdf);
    }

    private void givenDeliveredOrder(Instant deliveredAt) {
        when(userService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByIdAndUserIdAndStatusIn(
            ORDER_ID, 1L, List.of(OrderStatus.DELIVERED)
        )).thenReturn(Optional.of(order));
        when(orderStatusEventRepository.findFirstByOrderIdAndStatusOrderByOccurredAtAsc(
            ORDER_ID, OrderStatus.DELIVERED
        )).thenReturn(
            deliveredAt == null
                ? Optional.empty()
                : Optional.of(
                    OrderStatusEvent.builder()
                        .occurredAt(deliveredAt)
                        .build()
            )
        );
    }

    private void givenOrderInHistory() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByIdAndUserIdAndStatusIn(
            ORDER_ID, 1L, OrderStatus.HISTORY_STATUSES
        )).thenReturn(Optional.of(order));
    }

    private static CreateReturnRequest validRequest(int quantity) {
        return request(
            ReturnReason.DEFECTIVE, null, new CreateReturnItemRequest(ITEM_ID, quantity)
        );
    }

    private static CreateReturnRequest request(
        ReturnReason reason, String comment, CreateReturnItemRequest... items
    ) {
        return new CreateReturnRequest(reason, comment, List.of(items));
    }
}
