package com.d4rksoul.base.ecommerce.spring.react.integration.stripe;

import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeCheckoutEventResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeSessionResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidWebhookSignatureException;
import com.d4rksoul.base.ecommerce.spring.react.exception.StripeApiException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.StripeMapper;
import com.stripe.StripeClient;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StripeApiClient {

    private final StripeClient stripeClient;
    private final StripeMapper stripeMapper;

    @Value("${CORS_ORIGINS}")
    private String frontendUrl;
    @Value("${STRIPE_WEBHOOK_SECRET}")
    private String webhookSecret;

    public Optional<StripeCheckoutEventResult> parseCheckoutEvent(String payload, String signature) {
        Event event = constructEvent(payload, signature);

        if (!event.getType().startsWith("checkout.session.")) {
            return Optional.empty();
        }

        try {
            Session session = (Session) event.getDataObjectDeserializer().deserializeUnsafe();

            return Optional.of(stripeMapper.toCheckoutEvent(event.getType(), session));
        } catch (EventDataObjectDeserializationException e) {
            throw new StripeApiException("Could not deserialize Stripe event " + event.getId(), e);
        }
    }

    private Event constructEvent(String payload, String signature) {
        try {
            return Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new InvalidWebhookSignatureException("Invalid Stripe webhook signature.", e);
        }
    }

    public String createCustomer(User user, Order order) {
        try {
            return stripeClient.v1().customers()
                    .create(stripeMapper.toCustomerCreateParams(user, order))
                    .getId();
        } catch (StripeException e) {
            throw new StripeApiException("Failed to create customer with Stripe.", e);
        }
    }

    public StripeSessionResult createCheckoutSession(Order order, String customerId) {
        try {
            String couponId = null;
            if (order.getDiscountCode() != null && order.getDiscountPercentage() > 0) {
                couponId = stripeClient.v1().coupons().create(stripeMapper.toCouponCreateParams(order)).getId();
            }

            Session session = stripeClient.v1().checkout().sessions()
                    .create(stripeMapper.toSessionCreateParams(order, customerId, couponId, frontendUrl));

            return new StripeSessionResult(session.getId(), session.getUrl());
        } catch (StripeException e) {
            throw new StripeApiException("Failed to create checkout session with Stripe.", e);
        }
    }

    public void updateCustomerShipping(String customerId, Order order) {
        try {
            stripeClient.v1().customers().update(customerId, stripeMapper.toCustomerUpdateParams(order));
        } catch (StripeException e) {
            throw new StripeApiException("Failed to update customer with Stripe.", e);
        }
    }

    public void expireSession(String sessionId) {
        try {
            stripeClient.v1().checkout().sessions().expire(sessionId);
        } catch (StripeException e) {
            throw new StripeApiException("Failed to expire checkout session with Stripe.", e);
        }
    }

    public void expireSessionUnlessExpired(String sessionId) {
        try {
            boolean expired = "expired".equals(stripeClient.v1().checkout().sessions().retrieve(sessionId).getStatus());

            if (!expired) {
                expireSession(sessionId);
            }
        } catch (StripeException e) {
            throw new StripeApiException("Failed to retrieve checkout session from Stripe.", e);
        }
    }
}
