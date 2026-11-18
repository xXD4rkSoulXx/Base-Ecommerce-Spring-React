package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.CheckoutSessionResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeCheckoutEventResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeRefundResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeSessionResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.CouponCreateParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class StripeMapper {

    public CustomerCreateParams toCustomerCreateParams(User user, Order order) {
        return CustomerCreateParams.builder()
            .setEmail(user.getEmail())
            .setName(order.getRecipientName())
            .setShipping(
                CustomerCreateParams.Shipping.builder()
                    .setName(order.getRecipientName())
                    .setPhone(order.getPhone())
                    .setAddress(
                        CustomerCreateParams.Shipping.Address.builder()
                            .setLine1(order.getAddressLine1())
                            .setLine2(order.getAddressLine2())
                            .setCity(order.getCity())
                            .setState(order.getState())
                            .setPostalCode(order.getPostalCode())
                            .setCountry(order.getCountry())
                            .build()
                    )
                    .build()
            )
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
            .setExpiresAt(Instant.now().plus(Constants.CHECKOUT_SESSION_DURATION).getEpochSecond())
            .setAutomaticTax(
                SessionCreateParams.AutomaticTax.builder()
                    .setEnabled(true)
                    .build()
            )
            .setShippingAddressCollection(
                SessionCreateParams.ShippingAddressCollection.builder()
                    .addAllowedCountry(
                        SessionCreateParams.ShippingAddressCollection.AllowedCountry
                            .valueOf(order.getCountry().toUpperCase(Locale.ROOT))
                    )
                    .build()
            )
            .setCustomerUpdate(
                SessionCreateParams.CustomerUpdate.builder()
                    .setShipping(SessionCreateParams.CustomerUpdate.Shipping.AUTO)
                    .build()
            )
            .addShippingOption(
                SessionCreateParams.ShippingOption.builder()
                    .setShippingRateData(
                        SessionCreateParams.ShippingOption.ShippingRateData.builder()
                            .setType(SessionCreateParams.ShippingOption.ShippingRateData.Type.FIXED_AMOUNT)
                            .setDisplayName(order.getShippingCarrier() + " " + order.getShippingService())
                            .setTaxBehavior(
                                SessionCreateParams.ShippingOption.ShippingRateData.TaxBehavior.EXCLUSIVE
                            )
                            .setFixedAmount(
                                SessionCreateParams.ShippingOption.ShippingRateData.FixedAmount.builder()
                                    .setAmount(
                                        order.getShippingCost().setScale(2, RoundingMode.HALF_UP)
                                            .movePointRight(2).longValueExact()
                                    )
                                    .setCurrency(order.getCurrency())
                                    .build()
                            )
                            .build()
                    )
                    .build()
            );

        for (OrderItem item : order.getOrderItems()) {
            params.addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity((long) item.getQuantity())
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(order.getCurrency())
                            .setUnitAmount(
                                item.getPrice()
                                    .multiply(BigDecimal.valueOf(100L - item.getDiscountPercentage()))
                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                                    .setScale(2, RoundingMode.HALF_UP)
                                    .movePointRight(2).longValueExact()
                            )
                            .setTaxBehavior(SessionCreateParams.LineItem.PriceData.TaxBehavior.EXCLUSIVE)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(
                                        item.getProductName()
                                            + " (" + item.getColor()
                                            + ", " + item.getSize().getLabel() + ")"
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()
            );
        }

        if (couponId != null) {
            params.addDiscount(
                SessionCreateParams.Discount.builder()
                    .setCoupon(couponId)
                    .build()
            );
        }

        return params.build();
    }

    public StripeSessionResult toSessionResult(Session session) {
        return StripeSessionResult.builder()
            .id(session.getId())
            .url(session.getUrl())
            .build();
    }

    public StripeCheckoutEventResult toCheckoutEventResult(
        Event event, Session session, BigDecimal taxAmount, BigDecimal totalAmount
    ) {
        return StripeCheckoutEventResult.builder()
            .type(event.getType())
            .sessionId(session.getId())
            .paymentStatus(session.getPaymentStatus())
            .paymentIntentId(session.getPaymentIntent())
            .taxAmount(taxAmount)
            .totalAmount(totalAmount)
            .build();
    }

    public CheckoutSessionResponse toCheckoutSessionDto(StripeSessionResult session) {
        return CheckoutSessionResponse.builder()
            .url(session.url())
            .build();
    }

    public CustomerUpdateParams toCustomerUpdateParams(Order order) {
        return CustomerUpdateParams.builder()
            .setShipping(
                CustomerUpdateParams.Shipping.builder()
                    .setName(order.getRecipientName())
                    .setPhone(order.getPhone())
                    .setAddress(
                        CustomerUpdateParams.Shipping.Address.builder()
                            .setLine1(order.getAddressLine1())
                            .setLine2(order.getAddressLine2())
                            .setCity(order.getCity())
                            .setState(order.getState())
                            .setPostalCode(order.getPostalCode())
                            .setCountry(order.getCountry())
                            .build()
                    )
                    .build()
            )
            .build();
    }

    public RefundCreateParams toRefundCreateParams(
        Order order, OrderReturn orderReturn, BigDecimal amount
    ) {
        return RefundCreateParams.builder()
            .setPaymentIntent(order.getStripePaymentIntentId())
            .setAmount(amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact())
            .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
            .putMetadata("orderId", String.valueOf(order.getId()))
            .putMetadata("returnId", String.valueOf(orderReturn.getId()))
            .build();
    }

    public StripeRefundResult toRefundResult(Refund refund) {
        return StripeRefundResult.builder()
            .id(refund.getId())
            .status(refund.getStatus())
            .build();
    }
}
