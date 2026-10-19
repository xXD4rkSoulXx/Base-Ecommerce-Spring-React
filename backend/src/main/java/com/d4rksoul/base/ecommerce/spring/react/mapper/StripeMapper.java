package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeCheckoutEventResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.stripe.model.checkout.Session;
import com.stripe.param.CouponCreateParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class StripeMapper {

    public CustomerCreateParams toCustomerCreateParams(User user, Order order) {
        return CustomerCreateParams.builder()
                .setEmail(user.getEmail())
                .setName(order.getRecipientName())
                .setShipping(CustomerCreateParams.Shipping.builder()
                        .setName(order.getRecipientName())
                        .setPhone(order.getPhone())
                        .setAddress(CustomerCreateParams.Shipping.Address.builder()
                                .setLine1(order.getAddressLine1())
                                .setLine2(order.getAddressLine2())
                                .setCity(order.getCity())
                                .setState(order.getState())
                                .setPostalCode(order.getPostalCode())
                                .setCountry(order.getCountry())
                                .build())
                        .build())
                .build();
    }

    public CustomerUpdateParams toCustomerUpdateParams(Order order) {
        return CustomerUpdateParams.builder()
                .setShipping(CustomerUpdateParams.Shipping.builder()
                        .setName(order.getRecipientName())
                        .setPhone(order.getPhone())
                        .setAddress(CustomerUpdateParams.Shipping.Address.builder()
                                .setLine1(order.getAddressLine1())
                                .setLine2(order.getAddressLine2())
                                .setCity(order.getCity())
                                .setState(order.getState())
                                .setPostalCode(order.getPostalCode())
                                .setCountry(order.getCountry())
                                .build())
                        .build())
                .build();
    }

    public CouponCreateParams toCouponCreateParams(Order order) {
        return CouponCreateParams.builder()
                .setPercentOff(BigDecimal.valueOf(order.getDiscountPercentage()))
                .setDuration(CouponCreateParams.Duration.ONCE)
                .setName(order.getDiscountCode().getCode())
                .build();
    }

    public SessionCreateParams toSessionCreateParams(
            Order order, String customerId, String couponId, String frontendUrl
    ) {
        SessionCreateParams.Builder params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomer(customerId)
                .setClientReferenceId(String.valueOf(order.getId()))
                .putMetadata("orderId", String.valueOf(order.getId()))
                .setSuccessUrl(frontendUrl + "/checkout/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/cart")
                .setExpiresAt(Instant.now().plus(31, ChronoUnit.MINUTES).getEpochSecond())
                .setAutomaticTax(SessionCreateParams.AutomaticTax.builder().setEnabled(true).build())
                .setShippingAddressCollection(
                        SessionCreateParams.ShippingAddressCollection.builder()
                                .addAllowedCountry(
                                        SessionCreateParams.ShippingAddressCollection.AllowedCountry
                                                .valueOf(order.getCountry().toUpperCase()))
                                .build())
                .setCustomerUpdate(SessionCreateParams.CustomerUpdate.builder()
                        .setShipping(SessionCreateParams.CustomerUpdate.Shipping.AUTO)
                        .build())
                .addShippingOption(toShippingOption(order));

        for (OrderItem item : order.getOrderItems()) {
            params.addLineItem(toLineItem(order.getCurrency(), item));
        }

        if (couponId != null) {
            params.addDiscount(SessionCreateParams.Discount.builder().setCoupon(couponId).build());
        }

        return params.build();
    }

    private SessionCreateParams.ShippingOption toShippingOption(Order order) {
        return SessionCreateParams.ShippingOption.builder()
                .setShippingRateData(SessionCreateParams.ShippingOption.ShippingRateData.builder()
                        .setType(SessionCreateParams.ShippingOption.ShippingRateData.Type.FIXED_AMOUNT)
                        .setDisplayName(order.getShippingCarrier() + " " + order.getShippingService())
                        .setTaxBehavior(SessionCreateParams.ShippingOption.ShippingRateData.TaxBehavior.EXCLUSIVE)
                        .setFixedAmount(SessionCreateParams.ShippingOption.ShippingRateData.FixedAmount.builder()
                                .setAmount(toMinorUnits(order.getShippingCost()))
                                .setCurrency(order.getCurrency())
                                .build())
                        .build())
                .build();
    }

    private SessionCreateParams.LineItem toLineItem(String currency, OrderItem item) {
        BigDecimal unitPrice = item.getPrice()
                .multiply(BigDecimal.valueOf(100L - item.getDiscountPercentage()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return SessionCreateParams.LineItem.builder()
                .setQuantity((long) item.getQuantity())
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(currency)
                        .setUnitAmount(toMinorUnits(unitPrice))
                        .setTaxBehavior(SessionCreateParams.LineItem.PriceData.TaxBehavior.EXCLUSIVE)
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(item.getProductName() + " (" + item.getColor() + ", " + item.getSize() + ")")
                                .build())
                        .build())
                .build();
    }

    private long toMinorUnits(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
    }

    public StripeCheckoutEventResult toCheckoutEvent(String type, Session session) {
        return new StripeCheckoutEventResult(
                type,
                session.getId(),
                session.getPaymentStatus(),
                session.getPaymentIntent(),
                toTaxAmount(session)
        );
    }

    private BigDecimal toTaxAmount(Session session) {
        var totalDetails = session.getTotalDetails();

        if (totalDetails == null || totalDetails.getAmountTax() == null) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(totalDetails.getAmountTax()).movePointLeft(2);
    }
}
