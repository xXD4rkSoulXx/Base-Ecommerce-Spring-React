package com.d4rksoul.base.ecommerce.spring.react.mapper.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.CategoryUnitsProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.DailyTotalProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.TopDiscountCodeProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.TopProductProjection;
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
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PeriodTotalsResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
public class AdminDashboardMapper {

    public AdminDashboardSummaryResponse toSummaryDto(
        PeriodTotalsResult current, PeriodTotalsResult previous
    ) {
        return AdminDashboardSummaryResponse.builder()
            .netRevenue(current.grossRevenue().subtract(current.refunded()))
            .previousNetRevenue(previous.grossRevenue().subtract(previous.refunded()))
            .refunded(current.refunded())
            .previousRefunded(previous.refunded())
            .orders(current.orders())
            .previousOrders(previous.orders())
            .averageOrderValue(
                current.orders() == 0
                    ? BigDecimal.ZERO
                    : current.grossRevenue().divide(
                        BigDecimal.valueOf(current.orders()), 2, RoundingMode.HALF_UP
                    )
            )
            .previousAverageOrderValue(
                previous.orders() == 0
                    ? BigDecimal.ZERO
                    : previous.grossRevenue().divide(
                        BigDecimal.valueOf(previous.orders()), 2, RoundingMode.HALF_UP
                    )
            )
            .build();
    }

    public AdminDashboardSalesPointResponse toSalesPointDto(
        LocalDate date, DailyTotalProjection sales, DailyTotalProjection refunds
    ) {
        return AdminDashboardSalesPointResponse.builder()
            .date(date)
            .sales(sales == null ? BigDecimal.ZERO : sales.total())
            .refunds(refunds == null ? BigDecimal.ZERO : refunds.total())
            .orders(sales == null ? 0 : sales.count())
            .build();
    }

    public PeriodTotalsResult toPeriodTotals(BigDecimal revenue, BigDecimal refunds, long orders) {
        return PeriodTotalsResult.builder()
            .grossRevenue(revenue)
            .refunded(refunds)
            .orders(orders)
            .build();
    }

    public AdminDashboardCountResponse toCountDto(long count) {
        return AdminDashboardCountResponse.builder()
            .count(count)
            .build();
    }

    public AdminDashboardStatusCountResponse toStatusCountDto(
        OrderStatus status, long count
    ) {
        return AdminDashboardStatusCountResponse.builder()
            .status(status)
            .count(count)
            .build();
    }

    public AdminDashboardCategoryUnitsResponse toCategoryUnitsDto(
        CategoryUnitsProjection projection
    ) {
        return AdminDashboardCategoryUnitsResponse.builder()
            .category(projection.category())
            .unitsSold(projection.unitsSold())
            .build();
    }

    public AdminDashboardTopProductResponse toTopProductDto(
        TopProductProjection product, String cover
    ) {
        return AdminDashboardTopProductResponse.builder()
            .productId(product.productId())
            .ref(product.ref())
            .name(product.name())
            .cover(cover)
            .unitsSold(product.unitsSold())
            .build();
    }

    public AdminDashboardDiscountCodeResponse toDiscountCodeDto(
        TopDiscountCodeProjection projection
    ) {
        return AdminDashboardDiscountCodeResponse.builder()
            .code(projection.code())
            .orders(projection.orders())
            .build();
    }

    public AdminDashboardRecentOrderResponse toRecentOrderDto(Order order) {
        return AdminDashboardRecentOrderResponse.builder()
            .id(order.getId())
            .customerName(order.getUser().getFirstName() + " " + order.getUser().getLastName())
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .createdAt(order.getCreatedAt())
            .build();
    }

    public AdminDashboardReturnResponse toReturnDto(OrderReturn orderReturn) {
        return AdminDashboardReturnResponse.builder()
            .id(orderReturn.getId())
            .orderId(orderReturn.getOrder().getId())
            .customerName(
                orderReturn.getOrder().getUser().getFirstName()
                    + " "
                    + orderReturn.getOrder().getUser().getLastName()
            )
            .status(orderReturn.getStatus())
            .createdAt(orderReturn.getCreatedAt())
            .build();
    }

    public AdminDashboardLowStockResponse toLowStockDto(ProductSku sku) {
        return AdminDashboardLowStockResponse.builder()
            .skuId(sku.getId())
            .productId(sku.getProduct().getId())
            .productName(sku.getProduct().getName())
            .ref(sku.getProduct().getRef())
            .size(sku.getSize())
            .color(sku.getColor())
            .stock(sku.getStock())
            .build();
    }

    public AdminDashboardCustomersResponse toCustomersDto(
        long newCustomers, long previousNewCustomers, long totalCustomers
    ) {
        return AdminDashboardCustomersResponse.builder()
            .newCustomers(newCustomers)
            .previousNewCustomers(previousNewCustomers)
            .totalCustomers(totalCustomers)
            .build();
    }

    public AdminDashboardReviewsResponse toReviewsDto(
        long newReviews, long previousNewReviews, Double averageRating, Double previousAverageRating
    ) {
        return AdminDashboardReviewsResponse.builder()
            .newReviews(newReviews)
            .previousNewReviews(previousNewReviews)
            .averageRating(averageRating)
            .previousAverageRating(previousAverageRating)
            .build();
    }
}
