package com.d4rksoul.base.ecommerce.spring.react.integration.stripe;

import com.d4rksoul.base.ecommerce.spring.react.config.FrontendProperties;
import com.d4rksoul.base.ecommerce.spring.react.config.StripeProperties;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeCheckoutEventResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.exception.CheckoutInProgressException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidWebhookSignatureException;
import com.d4rksoul.base.ecommerce.spring.react.exception.StripeApiException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.StripeMapper;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Coupon;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.CouponCreateParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.RefundCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.HexFormat;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripeApiClientTest {

    private static final String WEBHOOK_SECRET = "whsec_test";
    private static final String FRONTEND_URL = "https://shop.example.com";

    private static final String NON_CHECKOUT_EVENT = """
        {"id": "evt_1", "object": "event", "type": "customer.created",
         "data": {"object": {"id": "cus_1", "object": "customer"}}}
        """;

    private static final String CHECKOUT_EVENT = """
        {"id": "evt_2", "object": "event", "type": "checkout.session.completed",
         "data": {"object": {"id": "cs_1", "object": "checkout.session", "payment_status": "paid",
                             "amount_total": 12345, "total_details": {"amount_tax": 2345}}}}
        """;

    private static final String CHECKOUT_EVENT_WITHOUT_TOTALS = """
        {"id": "evt_3", "object": "event", "type": "checkout.session.expired",
         "data": {"object": {"id": "cs_2", "object": "checkout.session"}}}
        """;

    @Mock
    private StripeMapper stripeMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StripeClient stripeClient;

    private StripeApiClient client;

    @BeforeEach
    void setUp() {
        client = new StripeApiClient(
            stripeMapper,
            stripeClient,
            new StripeProperties("sk_test", WEBHOOK_SECRET, "eur"),
            new FrontendProperties(FRONTEND_URL)
        );
    }

    @Test
    @DisplayName("webhook: an invalid signature becomes an InvalidWebhookSignatureException")
    void invalidSignatureIsRejected() {
        assertThatThrownBy(() -> client.parseCheckoutEvent(NON_CHECKOUT_EVENT, "t=1,v1=invalid"))
            .isInstanceOf(InvalidWebhookSignatureException.class);
    }

    @Test
    @DisplayName("webhook: events that are not checkout.session.* are ignored")
    void nonCheckoutEventIsIgnored() {
        assertThat(client.parseCheckoutEvent(NON_CHECKOUT_EVENT, signatureHeader(NON_CHECKOUT_EVENT))).isEmpty();

        verifyNoInteractions(stripeMapper);
    }

    @Test
    @DisplayName("webhook: converts tax and total from minor units to the main currency unit")
    void checkoutEventConvertsMinorUnits() {
        StripeCheckoutEventResult result = StripeCheckoutEventResult.builder().sessionId("cs_1").build();

        when(stripeMapper.toCheckoutEventResult(
            any(Event.class),
            any(Session.class),
            eq(new BigDecimal("23.45")),
            eq(new BigDecimal("123.45"))
        )).thenReturn(result);

        assertThat(client.parseCheckoutEvent(CHECKOUT_EVENT, signatureHeader(CHECKOUT_EVENT)))
            .contains(result);
    }

    @Test
    @DisplayName("webhook: a session without totals uses zero for tax and total")
    void checkoutEventWithoutTotalsUsesZero() {
        StripeCheckoutEventResult result = StripeCheckoutEventResult.builder().sessionId("cs_2").build();

        when(stripeMapper.toCheckoutEventResult(
            any(Event.class), any(Session.class), eq(BigDecimal.ZERO), eq(BigDecimal.ZERO)
        )).thenReturn(result);

        assertThat(client.parseCheckoutEvent(
            CHECKOUT_EVENT_WITHOUT_TOTALS, signatureHeader(CHECKOUT_EVENT_WITHOUT_TOTALS)
        )).contains(result);
    }

    @Test
    @DisplayName("expire: refuses to expire a session that is already complete")
    void expireSessionRefusesCompleteSession() throws StripeException {
        Session session = mock(Session.class);

        when(session.getStatus()).thenReturn("complete");
        when(stripeClient.v1().checkout().sessions().retrieve("cs_1")).thenReturn(session);

        assertThatThrownBy(() -> client.expireSession("cs_1"))
            .isInstanceOf(CheckoutInProgressException.class);

        verify(stripeClient.v1().checkout().sessions(), never()).expire("cs_1");
    }

    @ParameterizedTest(name = "status {0} -> expire called {1} time(s)")
    @CsvSource({"open, 1", "expired, 0"})
    @DisplayName("expire: only expires a session that is still open")
    void expireSessionOnlyWhenOpen(String status, int expireCalls) throws StripeException {
        Session session = mock(Session.class);

        when(session.getStatus()).thenReturn(status);
        when(stripeClient.v1().checkout().sessions().retrieve("cs_1")).thenReturn(session);

        client.expireSession("cs_1");

        verify(stripeClient.v1().checkout().sessions(), times(expireCalls)).expire("cs_1");
    }

    @Test
    @DisplayName("checkout session: creates a coupon and uses its id when the order has a discount")
    void checkoutSessionCreatesCouponForDiscountedOrder() throws StripeException {
        Order order = Order.builder()
            .id(7L)
            .discountCode(mock(DiscountCode.class))
            .discountPercentage(10)
            .build();

        CouponCreateParams couponParams = mock(CouponCreateParams.class);
        Coupon coupon = mock(Coupon.class);

        when(coupon.getId()).thenReturn("coupon_1");
        when(stripeMapper.toCouponCreateParams(order)).thenReturn(couponParams);
        when(stripeClient.v1().coupons().create(couponParams)).thenReturn(coupon);

        client.createCheckoutSession(order, "cus_1");

        verify(stripeMapper).toSessionCreateParams(
            order, "cus_1", "coupon_1", FRONTEND_URL
        );
    }

    @ParameterizedTest(name = "has code {0}, percentage {1}")
    @CsvSource({"false, 0", "false, 10", "true, 0"})
    @DisplayName("checkout session: does not create a coupon without both a discount code and a percentage")
    void checkoutSessionSkipsCouponWithoutDiscount(boolean hasCode, int percentage) {
        Order order = Order.builder()
            .id(7L)
            .discountCode(hasCode ? mock(DiscountCode.class) : null)
            .discountPercentage(percentage)
            .build();

        client.createCheckoutSession(order, "cus_1");

        verify(stripeMapper, never()).toCouponCreateParams(any());
        verify(stripeMapper).toSessionCreateParams(order, "cus_1", null, FRONTEND_URL);
    }

    @Test
    @DisplayName("refund: a cancellation refund is idempotent per order")
    void cancellationRefundUsesIdempotencyKeyPerOrder() throws StripeException {
        Order order = Order.builder()
            .id(7L)
            .build();

        RefundCreateParams params = mock(RefundCreateParams.class);

        when(stripeMapper.toCancellationRefundParams(order)).thenReturn(params);

        client.createCancellationRefund(order);

        ArgumentCaptor<RequestOptions> options = ArgumentCaptor.forClass(RequestOptions.class);
        verify(stripeClient.v1().refunds()).create(eq(params), options.capture());
        assertThat(options.getValue().getIdempotencyKey()).isEqualTo("cancel-refund-7");
    }

    @Test
    @DisplayName("refund: a return refund is idempotent per return")
    void returnRefundUsesIdempotencyKeyPerReturn() throws StripeException {
        Order order = Order.builder()
            .id(7L)
            .build();

        OrderReturn orderReturn = OrderReturn.builder()
            .id(3L)
            .build();

        BigDecimal amount = new BigDecimal("20.00");
        RefundCreateParams params = mock(RefundCreateParams.class);

        when(stripeMapper.toRefundCreateParams(order, orderReturn, amount)).thenReturn(params);

        client.createRefund(order, orderReturn, amount);

        ArgumentCaptor<RequestOptions> options = ArgumentCaptor.forClass(RequestOptions.class);
        verify(stripeClient.v1().refunds()).create(eq(params), options.capture());
        assertThat(options.getValue().getIdempotencyKey()).isEqualTo("return-refund-3");
    }

    @Test
    @DisplayName("a Stripe failure becomes a StripeApiException that keeps the cause")
    void stripeFailureBecomesStripeApiException() throws StripeException {
        User user = User.builder()
            .email("ana@mail.com")
            .build();

        Order order = Order.builder()
            .id(7L)
            .build();

        CustomerCreateParams params = mock(CustomerCreateParams.class);
        StripeException failure = mock(StripeException.class);

        when(stripeMapper.toCustomerCreateParams(user, order)).thenReturn(params);
        when(stripeClient.v1().customers().create(params)).thenThrow(failure);

        assertThatThrownBy(() -> client.createCustomer(user, order))
            .isInstanceOf(StripeApiException.class)
            .hasCause(failure);
    }

    private static String signatureHeader(String payload) {
        long timestamp = Instant.now().getEpochSecond();

        return "t=" + timestamp + ",v1=" + hmacSha256(timestamp + "." + payload);
    }

    private static String hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(WEBHOOK_SECRET.getBytes(UTF_8), "HmacSHA256"));

            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
