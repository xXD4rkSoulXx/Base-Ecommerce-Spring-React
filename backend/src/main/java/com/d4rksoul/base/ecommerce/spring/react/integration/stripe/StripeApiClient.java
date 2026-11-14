package com.d4rksoul.base.ecommerce.spring.react.integration.stripe;

import com.d4rksoul.base.ecommerce.spring.react.config.FrontendProperties;
import com.d4rksoul.base.ecommerce.spring.react.config.StripeProperties;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeCheckoutEventResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeRefundResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeSessionResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.exception.CheckoutInProgressException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidWebhookSignatureException;
import com.d4rksoul.base.ecommerce.spring.react.exception.StripeApiException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.StripeMapper;
import com.stripe.StripeClient;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Coupon;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.CouponCreateParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StripeApiClient {

    private final StripeMapper stripeMapper;
    private final StripeClient stripeClient;
    private final StripeProperties stripeProperties;
    private final FrontendProperties frontendProperties;

    public Optional<StripeCheckoutEventResult> parseCheckoutEvent(
        String payload, String signature
    ) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, stripeProperties.webhookSecret());
        } catch (SignatureVerificationException e) {
            throw new InvalidWebhookSignatureException("Invalid Stripe webhook signature. ", e);
        }

        if (!event.getType().startsWith("checkout.session.")) {
            return Optional.empty();
        }

        try {
            Session session = (Session) event.getDataObjectDeserializer().deserializeUnsafe();

            Long taxMinorUnits = session.getTotalDetails() != null
                ? session.getTotalDetails().getAmountTax()
                : null;

            BigDecimal taxAmount = taxMinorUnits != null
                ? BigDecimal.valueOf(taxMinorUnits).movePointLeft(2)
                : BigDecimal.ZERO;
            BigDecimal totalAmount = session.getAmountTotal() != null
                ? BigDecimal.valueOf(session.getAmountTotal()).movePointLeft(2)
                : BigDecimal.ZERO;

            return Optional.of(stripeMapper.toCheckoutEventResult(event, session, taxAmount, totalAmount));
        } catch (EventDataObjectDeserializationException e) {
            throw new StripeApiException("Could not deserialize Stripe event " + event.getId() + ". ", e);
        }
    }

    public String createCustomer(User user, Order order) {
        try {
            CustomerCreateParams params = stripeMapper.toCustomerCreateParams(user, order);
            Customer customer = stripeClient.v1().customers().create(params);

            return customer.getId();
        } catch (StripeException e) {
            throw new StripeApiException("Failed to create customer with Stripe. ", e);
        }
    }

    public StripeSessionResult createCheckoutSession(Order order, String customerId) {
        try {
            String couponId = null;
            if (order.getDiscountCode() != null && order.getDiscountPercentage() > 0) {
                CouponCreateParams couponParams = stripeMapper.toCouponCreateParams(order);
                Coupon coupon = stripeClient.v1().coupons().create(couponParams);
                couponId = coupon.getId();
            }

            SessionCreateParams sessionParams = stripeMapper.toSessionCreateParams(
                order, customerId, couponId, frontendProperties.url()
            );
            Session session = stripeClient.v1().checkout().sessions().create(sessionParams);

            return stripeMapper.toSessionResult(session);
        } catch (StripeException e) {
            throw new StripeApiException("Failed to create checkout session with Stripe. ", e);
        }
    }

    public void expireSession(String sessionId) {
        try {
            Session session = stripeClient.v1().checkout().sessions().retrieve(sessionId);

            if ("complete".equals(session.getStatus())) {
                throw new CheckoutInProgressException(
                    "A previous payment is still being confirmed. Please try again shortly."
                );
            }

            if (!"expired".equals(session.getStatus())) {
                stripeClient.v1().checkout().sessions().expire(sessionId);
            }
        } catch (StripeException e) {
            throw new StripeApiException("Failed to retrieve checkout session from Stripe. ", e);
        }
    }

    public void updateCustomerShipping(String customerId, Order order) {
        try {
            CustomerUpdateParams updateParams = stripeMapper.toCustomerUpdateParams(order);
            stripeClient.v1().customers().update(customerId, updateParams);
        } catch (StripeException e) {
            throw new StripeApiException("Failed to update customer with Stripe. ", e);
        }
    }

    public StripeRefundResult createRefund(
        Order order, OrderReturn orderReturn, BigDecimal amount
    ) {
        try {
            RequestOptions options = RequestOptions.builder()
                .setIdempotencyKey("return-refund-" + orderReturn.getId())
                .build();

            RefundCreateParams refundParams = stripeMapper.toRefundCreateParams(order, orderReturn, amount);
            Refund refund = stripeClient.v1().refunds().create(refundParams, options);

            return stripeMapper.toRefundResult(refund);
        } catch (StripeException e) {
            throw new StripeApiException("Failed to create refund with Stripe. ", e);
        }
    }
}
