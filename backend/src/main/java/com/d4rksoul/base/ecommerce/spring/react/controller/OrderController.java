package com.d4rksoul.base.ecommerce.spring.react.controller;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.OrderSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderSummaryResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<PageResponse<OrderSummaryResponse>> getOrders(
        @ModelAttribute OrderSearchRequest filter
    ) {
        PageResponse<OrderSummaryResponse> orders = orderService.getOrders(filter);

        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelPaidOrder(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailsResponse> getOrder(@PathVariable Long id) {
        OrderDetailsResponse order = orderService.getOrder(id);

        return ResponseEntity.ok(order);
    }
}
