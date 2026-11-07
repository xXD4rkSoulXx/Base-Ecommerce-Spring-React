package com.d4rksoul.base.ecommerce.spring.react.controller.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.UpdateShipmentStatusRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminOrderSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminUpdateOrderStatusRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminOrderDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminOrderSummaryResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    @PreAuthorize("hasAuthority('ORDERS_READ')")
    public ResponseEntity<PageResponse<AdminOrderSummaryResponse>> getOrders(
            @ModelAttribute AdminOrderSearchRequest filter
    ) {
        PageResponse<AdminOrderSummaryResponse> orders = adminOrderService.getOrders(filter);

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDERS_READ')")
    public ResponseEntity<AdminOrderDetailsResponse> getOrder(@PathVariable Long id) {
        AdminOrderDetailsResponse order = adminOrderService.getOrder(id);

        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ORDERS_WRITE')")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateOrderStatusRequest request
    ) {
        adminOrderService.changeStatus(id, request.status());

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{orderId}/shipments/{shipmentId}/status")
    @PreAuthorize("hasAuthority('ORDERS_WRITE')")
    public ResponseEntity<Void> changeShipmentStatus(
            @PathVariable Long orderId,
            @PathVariable Long shipmentId,
            @Valid @RequestBody UpdateShipmentStatusRequest request
    ) {
        adminOrderService.changeShipmentStatus(orderId, shipmentId, request.status());

        return ResponseEntity.noContent().build();
    }
}
