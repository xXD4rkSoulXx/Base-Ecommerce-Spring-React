package com.d4rksoul.base.ecommerce.spring.react.controller;

import com.d4rksoul.base.ecommerce.spring.react.integration.stripe.StripeApiClient;
import com.d4rksoul.base.ecommerce.spring.react.service.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeApiClient stripeApiClient;
    private final StripeWebhookService stripeWebhookService;

    @PostMapping("/stripe")
    public ResponseEntity<Void> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        stripeApiClient.parseCheckoutEvent(payload, signature).ifPresent(stripeWebhookService::handleEvent);

        return ResponseEntity.ok().build();
    }
}