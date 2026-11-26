package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductColor;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize;
import com.stripe.param.CouponCreateParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StripeMapperTest {

    private static final String FRONTEND_URL = "https://shop.example.com";

    private final StripeMapper mapper = new StripeMapper();

    @Test
    @DisplayName("customer: copies the email and the shipping address of the order")
    void customerParamsCopyEmailAndShipping() {
        User user = User.builder().email("ana@mail.com").build();

        CustomerCreateParams params = mapper.toCustomerCreateParams(
            user, order(item("50.00", 0, 1))
        );

        CustomerCreateParams.Shipping shipping = (CustomerCreateParams.Shipping) params.getShipping();
        CustomerCreateParams.Shipping.Address address = shipping.getAddress();

        assertThat(params.getEmail()).isEqualTo("ana@mail.com");
        assertThat(params.getName()).isEqualTo("Ana Silva");
        assertThat(shipping.getName()).isEqualTo("Ana Silva");
        assertThat(shipping.getPhone()).isEqualTo("+351912345678");
        assertThat(address.getLine1()).isEqualTo("Rua das Flores 1");
        assertThat(address.getCity()).isEqualTo("Lisboa");
        assertThat(address.getPostalCode()).isEqualTo("1000-001");
        assertThat(address.getCountry()).isEqualTo("PT");
    }

    @Test
    @DisplayName("coupon: uses the order percentage, the code as name and applies only once")
    void couponParamsUseOrderPercentage() {
        CouponCreateParams params = mapper.toCouponCreateParams(
            order(item("50.00", 0, 1))
        );

        assertThat(params.getPercentOff()).isEqualByComparingTo("20");
        assertThat(params.getName()).isEqualTo("SAVE");
        assertThat(params.getDuration()).isEqualTo(CouponCreateParams.Duration.ONCE);
    }

    @Test
    @DisplayName("session: links the checkout to the order and the customer")
    void sessionLinksToOrderAndCustomer() {
        SessionCreateParams params = session(null);

        assertThat(params.getMode()).isEqualTo(SessionCreateParams.Mode.PAYMENT);
        assertThat(params.getCustomer()).isEqualTo("cus_1");
        assertThat(params.getClientReferenceId()).isEqualTo("100");
        assertThat(metadata(params.getMetadata())).containsEntry("orderId", "100");
        assertThat(params.getSuccessUrl())
            .isEqualTo(FRONTEND_URL + "/checkout/success?session_id={CHECKOUT_SESSION_ID}");
        assertThat(params.getCancelUrl()).isEqualTo(FRONTEND_URL + "/cart");
    }

    @Test
    @DisplayName("session: expires after the configured checkout duration")
    void sessionExpiresAfterConfiguredDuration() {
        long earliest = Instant.now().plus(Constants.CHECKOUT_SESSION_DURATION).getEpochSecond();

        SessionCreateParams params = session(null);

        long latest = Instant.now().plus(Constants.CHECKOUT_SESSION_DURATION).getEpochSecond();
        assertThat(params.getExpiresAt()).isBetween(earliest, latest);
    }

    @Test
    @DisplayName("session: enables automatic tax and allows only the destination country")
    void sessionEnablesTaxAndRestrictsCountry() {
        SessionCreateParams params = session(null);

        assertThat(params.getAutomaticTax().getEnabled()).isTrue();
        assertThat(params.getShippingAddressCollection().getAllowedCountries())
            .containsExactly(SessionCreateParams.ShippingAddressCollection.AllowedCountry.PT);
    }

    @Test
    @DisplayName("session: sends the shipping cost in cents, without tax included")
    void sessionSendsShippingCostInCents() {
        SessionCreateParams params = session(null);

        SessionCreateParams.ShippingOption.ShippingRateData rate = params
            .getShippingOptions()
            .getFirst()
            .getShippingRateData();

        assertThat(rate.getDisplayName()).isEqualTo("CTT Standard");
        assertThat(rate.getFixedAmount().getAmount()).isEqualTo(490L);
        assertThat(rate.getFixedAmount().getCurrency()).isEqualTo("eur");
        assertThat(rate.getTaxBehavior())
            .isEqualTo(SessionCreateParams.ShippingOption.ShippingRateData.TaxBehavior.EXCLUSIVE);
    }

    @Test
    @DisplayName("session: sends one line per order item with quantity, currency and name")
    void sessionSendsOneLinePerItem() {
        Order order = order(
            item("50.00", 0, 3),
            item("20.00", 0, 1)
        );

        SessionCreateParams params = mapper.toSessionCreateParams(
            order, "cus_1", null, FRONTEND_URL
        );

        assertThat(params.getLineItems()).hasSize(2);

        SessionCreateParams.LineItem first = params.getLineItems().getFirst();
        assertThat(first.getQuantity()).isEqualTo(3L);
        assertThat(first.getPriceData().getCurrency()).isEqualTo("eur");
        assertThat(first.getPriceData().getTaxBehavior())
            .isEqualTo(SessionCreateParams.LineItem.PriceData.TaxBehavior.EXCLUSIVE);
        assertThat(first.getPriceData().getProductData().getName())
            .isEqualTo("Jacket (BLACK, " + ProductSize.M.getLabel() + ")");
    }

    @ParameterizedTest(name = "price {0} with {1}% off is {2} cents")
    @CsvSource({
        "50.00, 0, 5000",
        "50.00, 20, 4000",
        "19.99, 15, 1699",
        "10.05, 50, 503"
    })
    @DisplayName("session: sends the discounted unit price in cents, rounded half up")
    void sessionSendsDiscountedUnitPriceInCents(String price, int discount, long expectedCents) {
        SessionCreateParams params = mapper.toSessionCreateParams(
            order(item(price, discount, 1)), "cus_1", null, FRONTEND_URL
        );

        assertThat(params.getLineItems().getFirst().getPriceData().getUnitAmount()).isEqualTo(expectedCents);
    }

    @ParameterizedTest(name = "price {0} with {1}% off")
    @CsvSource({
        "50.00, 0",
        "50.00, 20",
        "19.99, 15",
        "10.05, 50",
        "0.99, 33",
        "100.00, 100"
    })
    @DisplayName("session: charges the same unit price the customer sees in the order")
    void sessionChargesTheSamePriceTheOrderShows(String price, int discount) {
        OrderItem item = item(price, discount, 1);

        SessionCreateParams params = mapper.toSessionCreateParams(
            order(item), "cus_1", null, FRONTEND_URL
        );

        long shownInCents = new OrderItemMapper().toDto(item, 0).unitPrice()
            .movePointRight(2).longValueExact();
        long chargedInCents = params.getLineItems().getFirst().getPriceData().getUnitAmount();
        assertThat(chargedInCents).isEqualTo(shownInCents);
    }

    @Test
    @DisplayName("session: applies the coupon when there is one")
    void sessionAppliesCouponWhenPresent() {
        SessionCreateParams params = session("coupon_1");

        assertThat(params.getDiscounts()).hasSize(1);
        assertThat(params.getDiscounts().getFirst().getCoupon()).isEqualTo("coupon_1");
    }

    @Test
    @DisplayName("session: sends no discount when there is no coupon")
    void sessionSendsNoDiscountWithoutCoupon() {
        SessionCreateParams params = session(null);

        assertThat(params.getDiscounts()).isNullOrEmpty();
    }

    @Test
    @DisplayName("cancellation refund: refunds the whole order total in cents")
    void cancellationRefundUsesWholeTotal() {
        Order order = order(item("50.00", 0, 1));
        order.setTotalAmount(new BigDecimal("52.30"));

        RefundCreateParams params = mapper.toCancellationRefundParams(order);

        assertThat(params.getPaymentIntent()).isEqualTo("pi_1");
        assertThat(params.getAmount()).isEqualTo(5230L);
        assertThat(params.getReason()).isEqualTo(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);
        assertThat(metadata(params.getMetadata())).containsEntry("orderId", "100");
    }

    @Test
    @DisplayName("return refund: refunds the given amount in cents and links the return")
    void returnRefundUsesGivenAmount() {
        OrderReturn orderReturn = OrderReturn.builder().id(7L).build();

        RefundCreateParams params = mapper.toRefundCreateParams(
            order(item("50.00", 0, 1)),
            orderReturn, new BigDecimal("12.5")
        );

        assertThat(params.getPaymentIntent()).isEqualTo("pi_1");
        assertThat(params.getAmount()).isEqualTo(1250L);
        assertThat(metadata(params.getMetadata()))
            .containsEntry("orderId", "100")
            .containsEntry("returnId", "7");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> metadata(Object metadata) {
        return (Map<String, String>) metadata;
    }

    private SessionCreateParams session(String couponId) {
        return mapper.toSessionCreateParams(
            order(item("50.00", 0, 1)),
            "cus_1",
            couponId,
            FRONTEND_URL
        );
    }

    private static Order order(OrderItem... items) {
        return Order.builder()
            .id(100L)
            .currency("eur")
            .discountPercentage(20)
            .discountCode(DiscountCode.builder().code("SAVE").build())
            .shippingCost(new BigDecimal("4.90"))
            .shippingCarrier("CTT")
            .shippingService("Standard")
            .stripePaymentIntentId("pi_1")
            .recipientName("Ana Silva")
            .phone("+351912345678")
            .addressLine1("Rua das Flores 1")
            .city("Lisboa")
            .postalCode("1000-001")
            .country("PT")
            .orderItems(new ArrayList<>(List.of(items)))
            .build();
    }

    private static OrderItem item(String price, int discountPercentage, int quantity) {
        Product product = Product.builder().ref("REF-1").build();
        ProductSku sku = ProductSku.builder().product(product).build();

        return OrderItem.builder()
            .id(1L)
            .productSku(sku)
            .productName("Jacket")
            .color(ProductColor.BLACK)
            .size(ProductSize.M)
            .price(new BigDecimal(price))
            .discountPercentage(discountPercentage)
            .quantity(quantity)
            .build();
    }
}
