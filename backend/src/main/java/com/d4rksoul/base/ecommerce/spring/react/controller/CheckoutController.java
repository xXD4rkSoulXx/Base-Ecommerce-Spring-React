package com.d4rksoul.base.ecommerce.spring.react.controller;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.CheckoutAddressRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.CreateOrderRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.CheckoutSessionResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ShippingRateResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.PaymentService;
import com.d4rksoul.base.ecommerce.spring.react.service.ShippingRateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final ShippingRateService shippingRateService;
    private final PaymentService paymentService;

    @PostMapping("/shipping-rates")
    public ResponseEntity<List<ShippingRateResponse>> getShippingRates(
            @Valid @RequestBody CheckoutAddressRequest request
    ) {
        List<ShippingRateResponse> rates = shippingRateService.getShippingRates(request);

        return ResponseEntity.ok(rates);
    }

    @PostMapping("/session")
    public ResponseEntity<CheckoutSessionResponse> createSession(@Valid @RequestBody CreateOrderRequest request) {
        CheckoutSessionResponse session = paymentService.createCheckoutSession(request);

        return ResponseEntity.ok(session);
    }
}
