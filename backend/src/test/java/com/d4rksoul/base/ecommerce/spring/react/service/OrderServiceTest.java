package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.config.StripeProperties;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.CheckoutAddressRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.CreateOrderRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ShippingRateResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeRefundResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Cart;
import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.EmptyCartException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InsufficientStockException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidDiscountCodeException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidOrderTransitionException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidShippingRateException;
import com.d4rksoul.base.ecommerce.spring.react.exception.OrderNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductUnavailableException;
import com.d4rksoul.base.ecommerce.spring.react.exception.RefundFailedException;
import com.d4rksoul.base.ecommerce.spring.react.exception.StripeApiException;
import com.d4rksoul.base.ecommerce.spring.react.integration.stripe.StripeApiClient;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderItemMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final long USER_ID = 1L;
    private static final long CART_ID = 2L;
    private static final long SKU_ID = 3L;
    private static final long ORDER_ID = 100L;
    private static final String RATE_ID = "rate_1";
    private static final int DISCOUNT_PERCENTAGE = 20;

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductSkuRepository productSkuRepository;
    @Mock
    private UserService userService;
    @Mock
    private ProductSkuImageService productSkuImageService;
    @Mock
    private ShippingRateService shippingRateService;
    @Mock
    private OrderStatusEventService orderStatusEventService;
    @Mock
    private StripeApiClient stripeApiClient;
    @Mock
    private StripeProperties stripeProperties;
    @Spy
    private OrderMapper orderMapper = new OrderMapper();
    @Spy
    private OrderItemMapper orderItemMapper = new OrderItemMapper();

    @InjectMocks
    private OrderService service;

    private final User user = User.builder()
        .id(USER_ID)
        .build();

    private final Cart cart = Cart.builder()
        .id(CART_ID)
        .user(user)
        .build();

    private final CheckoutAddressRequest address = new CheckoutAddressRequest(
        "Ana Silva",
        "Rua das Flores 1",
        null,
        "Lisboa",
        null,
        "1000-001",
        "PT",
        "+351912345678"
    );
    private final CreateOrderRequest request = new CreateOrderRequest(address, RATE_ID);

    @Test
    @DisplayName("rejects checkout when the user has no cart")
    void createRejectsMissingCart() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserIdForUpdate(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createPendingOrder(request))
            .isInstanceOf(EmptyCartException.class);
    }

    @Test
    @DisplayName("rejects checkout when the cart has no items")
    void createRejectsEmptyCart() {
        givenCart(List.of());

        assertThatThrownBy(() -> service.createPendingOrder(request))
            .isInstanceOf(EmptyCartException.class);
    }

    @Test
    @DisplayName("rejects checkout when a SKU in the cart is inactive")
    void createRejectsInactiveSku() {
        givenCart(List.of(cartItem(false, true, 1)));

        assertThatThrownBy(() -> service.createPendingOrder(request))
            .isInstanceOf(ProductUnavailableException.class)
            .hasMessageContaining("Jacket");
    }

    @Test
    @DisplayName("rejects checkout when a product in the cart is inactive")
    void createRejectsInactiveProduct() {
        givenCart(List.of(cartItem(true, false, 1)));

        assertThatThrownBy(() -> service.createPendingOrder(request))
            .isInstanceOf(ProductUnavailableException.class);
    }

    @Test
    @DisplayName("rejects a shipping rate that is not offered anymore")
    void createRejectsUnknownRate() {
        givenCart(List.of(cartItem(true, true, 1)));

        when(shippingRateService.getShippingRates(address))
            .thenReturn(List.of(rate("other_rate", "eur")));

        assertThatThrownBy(() -> service.createPendingOrder(request))
            .isInstanceOf(InvalidShippingRateException.class)
            .hasMessageContaining("no longer available");
    }

    @Test
    @DisplayName("rejects a shipping rate in a different currency than the store")
    void createRejectsRateWithOtherCurrency() {
        givenCart(List.of(cartItem(true, true, 1)));

        when(shippingRateService.getShippingRates(address))
            .thenReturn(List.of(rate(RATE_ID, "usd")));
        when(stripeProperties.currency()).thenReturn("eur");

        assertThatThrownBy(() -> service.createPendingOrder(request))
            .isInstanceOf(InvalidShippingRateException.class)
            .hasMessageContaining("currency");
    }

    @Test
    @DisplayName("accepts the rate currency in any letter case")
    void createAcceptsRateCurrencyInAnyCase() {
        givenValidCheckout(1);

        when(shippingRateService.getShippingRates(address))
            .thenReturn(List.of(rate(RATE_ID, "EUR")));

        Order order = service.createPendingOrder(request);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("rejects a discount code the user already used")
    void createRejectsAlreadyUsedDiscountCode() {
        DiscountCode code = discountCode(true);

        cart.setDiscountCode(code);

        givenCart(List.of(cartItem(true, true, 1)));
        givenRate();

        when(orderRepository.existsByUserIdAndDiscountCodeIdAndStatusNotIn(
            USER_ID, code.getId(), OrderStatus.DISCOUNT_UNUSED_STATUSES
        )).thenReturn(true);

        assertThatThrownBy(() -> service.createPendingOrder(request))
            .isInstanceOf(InvalidDiscountCodeException.class)
            .hasMessageContaining("already used");

        verify(productSkuRepository, never()).decrementStock(any(), anyInt());
    }

    @Test
    @DisplayName("silently ignores an inactive discount code")
    void createIgnoresInactiveDiscountCode() {
        cart.setDiscountCode(discountCode(false));

        givenValidCheckout(1);

        Order order = service.createPendingOrder(request);

        assertThat(order.getDiscountCode()).isNull();
        assertThat(order.getDiscountPercentage()).isZero();

        verify(orderRepository, never()).existsByUserIdAndDiscountCodeIdAndStatusNotIn(any(), any(), any());
    }

    @Test
    @DisplayName("snapshots an active discount code into the order")
    void createAppliesActiveDiscountCode() {
        DiscountCode code = discountCode(true);

        cart.setDiscountCode(code);

        givenValidCheckout(1);

        when(orderRepository.existsByUserIdAndDiscountCodeIdAndStatusNotIn(
            USER_ID, code.getId(), OrderStatus.DISCOUNT_UNUSED_STATUSES
        )).thenReturn(false);

        Order order = service.createPendingOrder(request);

        assertThat(order.getDiscountCode()).isSameAs(code);
        assertThat(order.getDiscountPercentage()).isEqualTo(DISCOUNT_PERCENTAGE);
    }

    @Test
    @DisplayName("expires previous pending orders and gives their stock back")
    void createReleasesPreviousPendingOrders() {
        givenValidCheckout(1);

        Order previous = Order.builder()
            .id(50L)
            .stripeCheckoutSessionId("cs_old")
            .orderItems(new ArrayList<>(List.of(
                OrderItem.builder()
                    .productSku(
                        ProductSku.builder()
                            .id(7L)
                            .build()
                    )
                    .quantity(2)
                    .build()
            )))
            .build();

        when(orderRepository.findByUserIdAndStatus(USER_ID, OrderStatus.PENDING))
            .thenReturn(List.of(previous));
        when(orderRepository.updateStatus(50L, OrderStatus.PENDING, OrderStatus.CANCELED))
            .thenReturn(1);

        service.createPendingOrder(request);

        verify(stripeApiClient).expireSession("cs_old");
        verify(productSkuRepository).incrementStock(7L, 2);
    }

    @Test
    @DisplayName("rejects checkout when there is not enough stock")
    void createRejectsInsufficientStock() {
        givenCart(List.of(cartItem(true, true, 4)));
        givenRate();

        when(productSkuRepository.decrementStock(SKU_ID, 4)).thenReturn(0);

        assertThatThrownBy(() -> service.createPendingOrder(request))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessageContaining("Jacket");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("creates a pending order with the shipping rate and the item snapshots")
    void createBuildsPendingOrder() {
        givenValidCheckout(2);

        Order order = service.createPendingOrder(request);
        List<OrderItem> items = order.getOrderItems();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getCurrency()).isEqualTo("eur");
        assertThat(order.getShippingCost()).isEqualByComparingTo("4.90");
        assertThat(order.getDiscountPercentage()).isZero();
        assertThat(order.getRecipientName()).isEqualTo("Ana Silva");
        assertThat(items).hasSize(1);
        assertThat(items.getFirst().getQuantity()).isEqualTo(2);
        assertThat(items.getFirst().getPrice()).isEqualByComparingTo("50.00");

        verify(productSkuRepository).decrementStock(SKU_ID, 2);
    }

    @Test
    @DisplayName("gives the stock back when a pending order is released")
    void releaseReservationGivesStockBack() {
        Order order = orderWithItem(new BigDecimal("100.00"));

        when(orderRepository.updateStatus(ORDER_ID, OrderStatus.PENDING, OrderStatus.CANCELED))
            .thenReturn(1);

        service.releaseReservation(order, OrderStatus.CANCELED);

        verify(productSkuRepository).incrementStock(SKU_ID, 2);
    }

    @Test
    @DisplayName("does not give the stock back twice when the order is no longer pending")
    void releaseReservationDoesNothingWhenNotPending() {
        Order order = orderWithItem(new BigDecimal("100.00"));

        when(orderRepository.updateStatus(
            ORDER_ID, OrderStatus.PENDING, OrderStatus.CANCELED
        )).thenReturn(0);

        service.releaseReservation(order, OrderStatus.CANCELED);

        verify(productSkuRepository, never()).incrementStock(any(), anyInt());
    }

    @Test
    @DisplayName("cancel paid: rejects an order that is not the user's or not visible")
    void cancelPaidRejectsUnknownOrder() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByIdAndUserIdAndStatusIn(
            ORDER_ID, USER_ID, OrderStatus.VISIBLE_STATUSES
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelPaidOrder(ORDER_ID))
            .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("cancel paid: rejects an order that is no longer paid")
    void cancelPaidRejectsOrderThatIsNotPaid() {
        givenPaidOrder(new BigDecimal("100.00"));

        when(orderStatusEventService.transition(
            ORDER_ID, OrderStatus.PAID, OrderStatus.CANCELED_REFUNDED
        )).thenReturn(false);

        assertThatThrownBy(() -> service.cancelPaidOrder(ORDER_ID))
            .isInstanceOf(InvalidOrderTransitionException.class);

        verify(productSkuRepository, never()).incrementStock(any(), anyInt());
        verifyNoInteractions(stripeApiClient);
    }

    @Test
    @DisplayName("cancel paid: gives the stock back and refunds the order")
    void cancelPaidRestocksAndRefunds() {
        Order order = givenPaidOrder(new BigDecimal("100.00"));

        givenTransitionSucceeds();

        when(stripeApiClient.createCancellationRefund(order))
            .thenReturn(new StripeRefundResult("re_1", "succeeded"));

        service.cancelPaidOrder(ORDER_ID);

        verify(productSkuRepository).incrementStock(SKU_ID, 2);
        verify(stripeApiClient).createCancellationRefund(order);
    }

    @Test
    @DisplayName("cancel paid: skips the refund when the order total is zero")
    void cancelPaidSkipsRefundForFreeOrder() {
        givenPaidOrder(BigDecimal.ZERO);
        givenTransitionSucceeds();

        service.cancelPaidOrder(ORDER_ID);

        verify(productSkuRepository).incrementStock(SKU_ID, 2);
        verifyNoInteractions(stripeApiClient);
    }

    @Test
    @DisplayName("cancel paid: reports a refund when Stripe throws")
    void cancelPaidReportsStripeError() {
        Order order = givenPaidOrder(new BigDecimal("100.00"));

        givenTransitionSucceeds();

        when(stripeApiClient.createCancellationRefund(order))
            .thenThrow(new StripeApiException("Stripe down", new RuntimeException()));

        assertThatThrownBy(() -> service.cancelPaidOrder(ORDER_ID))
            .isInstanceOf(RefundFailedException.class);
    }

    @Test
    @DisplayName("cancel paid: reports a refund that Stripe marks as failed or canceled")
    void cancelPaidReportsFailedRefundStatus() {
        Order order = givenPaidOrder(new BigDecimal("100.00"));

        givenTransitionSucceeds();

        when(stripeApiClient.createCancellationRefund(order))
            .thenReturn(new StripeRefundResult("re_1", "failed"));

        assertThatThrownBy(() -> service.cancelPaidOrder(ORDER_ID))
            .isInstanceOf(RefundFailedException.class);
    }

    private void givenCart(List<CartItem> items) {
        when(userService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserIdForUpdate(USER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByIdAsc(CART_ID)).thenReturn(items);
    }

    private void givenRate() {
        when(shippingRateService.getShippingRates(address))
            .thenReturn(List.of(rate(RATE_ID, "eur")));
        when(stripeProperties.currency()).thenReturn("eur");
    }

    private void givenValidCheckout(int quantity) {
        givenCart(List.of(cartItem(true, true, quantity)));
        givenRate();

        when(productSkuRepository.decrementStock(SKU_ID, quantity)).thenReturn(1);
        when(orderRepository.save(any(Order.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Order givenPaidOrder(BigDecimal total) {
        Order order = orderWithItem(total);

        when(userService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByIdAndUserIdAndStatusIn(ORDER_ID, USER_ID, OrderStatus.VISIBLE_STATUSES))
            .thenReturn(Optional.of(order));

        return order;
    }

    private void givenTransitionSucceeds() {
        when(orderStatusEventService.transition(ORDER_ID, OrderStatus.PAID, OrderStatus.CANCELED_REFUNDED))
            .thenReturn(true);
    }

    private static Order orderWithItem(BigDecimal total) {
        return Order.builder()
            .id(ORDER_ID)
            .totalAmount(total)
            .orderItems(new ArrayList<>(List.of(
                OrderItem.builder()
                    .productSku(
                        ProductSku.builder()
                            .id(SKU_ID)
                            .build()
                    )
                    .quantity(2)
                    .build()
            )))
            .build();
    }

    private static CartItem cartItem(boolean skuActive, boolean productActive, int quantity) {
        Product product = Product.builder()
            .name("Jacket")
            .ref("REF-1")
            .price(new BigDecimal("50.00"))
            .active(productActive)
            .build();

        ProductSku sku = ProductSku.builder()
            .id(SKU_ID)
            .product(product)
            .active(skuActive)
            .build();

        return CartItem.builder().productSku(sku).quantity(quantity).build();
    }

    private static DiscountCode discountCode(boolean active) {
        return DiscountCode.builder()
            .id(5L)
            .code("SAVE")
            .discountPercentage(DISCOUNT_PERCENTAGE)
            .active(active)
            .build();
    }

    private static ShippingRateResponse rate(String rateId, String currency) {
        return ShippingRateResponse.builder()
            .rateId(rateId)
            .carrier("CTT")
            .service("Standard")
            .amount(new BigDecimal("4.90"))
            .currency(currency)
            .build();
    }
}

