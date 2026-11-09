package com.d4rksoul.base.ecommerce.spring.react.controller.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminDashboardPeriodRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminAuditLogResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardCategoryUnitsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardCountResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardCustomersResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardDiscountCodeResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardLowStockResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardRecentOrderResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardReturnResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardReviewsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardSalesPointResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardStatusCountResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardSummaryResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardTopProductResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminDashboardCommunityService;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminDashboardOperationsService;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminDashboardSalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardSalesService adminDashboardSalesService;
    private final AdminDashboardOperationsService adminDashboardOperationsService;
    private final AdminDashboardCommunityService adminDashboardCommunityService;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ORDERS_READ')")
    public ResponseEntity<AdminDashboardSummaryResponse> getSummary(AdminDashboardPeriodRequest period) {
        AdminDashboardSummaryResponse summary = adminDashboardSalesService.getSummary(period);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/sales")
    @PreAuthorize("hasAuthority('ORDERS_READ')")
    public ResponseEntity<List<AdminDashboardSalesPointResponse>> getSales(AdminDashboardPeriodRequest period) {
        List<AdminDashboardSalesPointResponse> sales = adminDashboardSalesService.getSales(period);

        return ResponseEntity.ok(sales);
    }

    @GetMapping("/orders-by-status")
    @PreAuthorize("hasAuthority('ORDERS_READ')")
    public ResponseEntity<List<AdminDashboardStatusCountResponse>> getOrdersByStatus(
            AdminDashboardPeriodRequest period
    ) {
        List<AdminDashboardStatusCountResponse> ordersByStatus = adminDashboardSalesService
                .getOrdersByStatus(period);

        return ResponseEntity.ok(ordersByStatus);
    }

    @GetMapping("/sales-by-category")
    @PreAuthorize("hasAuthority('ORDERS_READ')")
    public ResponseEntity<List<AdminDashboardCategoryUnitsResponse>> getSalesByCategory(
            AdminDashboardPeriodRequest period
    ) {
        List<AdminDashboardCategoryUnitsResponse> salesByCategory = adminDashboardSalesService
                .getSalesByCategory(period);

        return ResponseEntity.ok(salesByCategory);
    }

    @GetMapping("/recent-orders")
    @PreAuthorize("hasAuthority('ORDERS_READ')")
    public ResponseEntity<List<AdminDashboardRecentOrderResponse>> getRecentOrders() {
        List<AdminDashboardRecentOrderResponse> recentOrders = adminDashboardSalesService.getRecentOrders();

        return ResponseEntity.ok(recentOrders);
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasAuthority('PRODUCTS_READ')")
    public ResponseEntity<List<AdminDashboardTopProductResponse>> getTopProducts(
            AdminDashboardPeriodRequest period
    ) {
        List<AdminDashboardTopProductResponse> topProducts = adminDashboardSalesService.getTopProducts(period);

        return ResponseEntity.ok(topProducts);
    }

    @GetMapping("/top-discount-codes")
    @PreAuthorize("hasAuthority('DISCOUNTS_READ')")
    public ResponseEntity<List<AdminDashboardDiscountCodeResponse>> getTopDiscountCodes(
            AdminDashboardPeriodRequest period
    ) {
        List<AdminDashboardDiscountCodeResponse> topDiscountCodes = adminDashboardSalesService
                .getTopDiscountCodes(period);

        return ResponseEntity.ok(topDiscountCodes);
    }

    @GetMapping("/pending-orders")
    @PreAuthorize("hasAuthority('ORDERS_READ')")
    public ResponseEntity<AdminDashboardCountResponse> getPendingOrders() {
        AdminDashboardCountResponse pendingOrders = adminDashboardOperationsService.getPendingOrders();

        return ResponseEntity.ok(pendingOrders);
    }

    @GetMapping("/pending-returns")
    @PreAuthorize("hasAuthority('RETURNS_READ')")
    public ResponseEntity<AdminDashboardCountResponse> getPendingReturns() {
        AdminDashboardCountResponse pendingReturns = adminDashboardOperationsService.getPendingReturns();

        return ResponseEntity.ok(pendingReturns);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('STOCK_READ')")
    public ResponseEntity<AdminDashboardCountResponse> getLowStock() {
        AdminDashboardCountResponse lowStock = adminDashboardOperationsService.getLowStock();

        return ResponseEntity.ok(lowStock);
    }

    @GetMapping("/returns-to-handle")
    @PreAuthorize("hasAuthority('RETURNS_READ')")
    public ResponseEntity<List<AdminDashboardReturnResponse>> getReturnsToHandle() {
        List<AdminDashboardReturnResponse> returnsToHandle = adminDashboardOperationsService.getReturnsToHandle();

        return ResponseEntity.ok(returnsToHandle);
    }

    @GetMapping("/lowest-stock")
    @PreAuthorize("hasAuthority('STOCK_READ')")
    public ResponseEntity<List<AdminDashboardLowStockResponse>> getLowestStock() {
        List<AdminDashboardLowStockResponse> lowestStock = adminDashboardOperationsService.getLowestStock();

        return ResponseEntity.ok(lowestStock);
    }

    @GetMapping("/customers")
    @PreAuthorize("hasAuthority('CUSTOMERS_READ')")
    public ResponseEntity<AdminDashboardCustomersResponse> getCustomers(AdminDashboardPeriodRequest period) {
        AdminDashboardCustomersResponse customers = adminDashboardCommunityService.getCustomers(period);

        return ResponseEntity.ok(customers);
    }

    @GetMapping("/reviews")
    @PreAuthorize("hasAuthority('REVIEWS_READ')")
    public ResponseEntity<AdminDashboardReviewsResponse> getReviews(AdminDashboardPeriodRequest period) {
        AdminDashboardReviewsResponse reviews = adminDashboardCommunityService.getReviews(period);

        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/recent-activity")
    @PreAuthorize("hasAuthority('AUDIT_READ')")
    public ResponseEntity<List<AdminAuditLogResponse>> getRecentActivity() {
        List<AdminAuditLogResponse> recentActivity = adminDashboardCommunityService.getRecentActivity();

        return ResponseEntity.ok(recentActivity);
    }
}
