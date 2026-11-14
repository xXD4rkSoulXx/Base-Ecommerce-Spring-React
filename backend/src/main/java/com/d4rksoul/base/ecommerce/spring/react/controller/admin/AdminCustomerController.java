package com.d4rksoul.base.ecommerce.spring.react.controller.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminCustomerOrderSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminCustomerSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderSummaryResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminCustomerResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMERS_READ')")
    public ResponseEntity<PageResponse<AdminCustomerResponse>> getCustomers(
        @ModelAttribute AdminCustomerSearchRequest filter
    ) {
        PageResponse<AdminCustomerResponse> customers = adminCustomerService.getCustomers(filter);

        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMERS_READ')")
    public ResponseEntity<AdminCustomerResponse> getCustomer(@PathVariable Long id) {
        AdminCustomerResponse customer = adminCustomerService.getCustomer(id);

        return ResponseEntity.ok(customer);
    }

    @GetMapping("/{id}/orders")
    @PreAuthorize("hasAuthority('CUSTOMERS_READ') and hasAuthority('ORDERS_READ')")
    public ResponseEntity<PageResponse<OrderSummaryResponse>> getCustomerOrders(
        @PathVariable Long id, @ModelAttribute AdminCustomerOrderSearchRequest filter
    ) {
        PageResponse<OrderSummaryResponse> orders = adminCustomerService.getCustomerOrders(id, filter);

        return ResponseEntity.ok(orders);
    }
}
