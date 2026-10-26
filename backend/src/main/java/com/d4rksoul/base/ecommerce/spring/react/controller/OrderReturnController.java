package com.d4rksoul.base.ecommerce.spring.react.controller;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.CreateReturnRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderReturnResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders/{orderId}/returns")
@RequiredArgsConstructor
public class OrderReturnController {

    private final OrderReturnService orderReturnService;

    @PostMapping
    public ResponseEntity<OrderReturnResponse> requestReturn(
            @PathVariable Long orderId,
            @Valid @RequestBody CreateReturnRequest request
    ) {
        OrderReturnResponse response = orderReturnService.requestReturn(orderId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
