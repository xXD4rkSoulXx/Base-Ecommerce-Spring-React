package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.DailyTotalProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.StatusCountProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.TopProductProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminDashboardPeriodRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardCategoryUnitsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardDiscountCodeResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardRecentOrderResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardSalesPointResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardStatusCountResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardSummaryResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardTopProductResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.PeriodTotalsResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminDashboardMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.ProductSkuImageService;
import com.d4rksoul.base.ecommerce.spring.react.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardSalesService {

    private final OrderRepository orderRepository;
    private final OrderReturnRepository orderReturnRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductSkuImageService productSkuImageService;
    private final AdminDashboardMapper adminDashboardMapper;

    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getSummary(AdminDashboardPeriodRequest period) {
        AdminDashboardSupport.Period range = AdminDashboardSupport.periodOf(period);

        return adminDashboardMapper.toSummaryDto(
            totals(range.from(), range.to()),
            totals(range.previousFrom(), range.from())
        );
    }

    @Transactional(readOnly = true)
    public List<AdminDashboardSalesPointResponse> getSales(AdminDashboardPeriodRequest period) {
        AdminDashboardSupport.Period range = AdminDashboardSupport.periodOf(period);

        Map<LocalDate, DailyTotalProjection> sales = orderRepository
            .sumTotalAmountByDay(OrderStatus.HISTORY_STATUSES, range.from(), range.to()).stream()
            .collect(Collectors.toMap(day ->
                    LocalDate.of(day.year(), day.month(), day.day()),
                    Function.identity()
            ));

        Map<LocalDate, DailyTotalProjection> refunds = orderReturnRepository
            .sumRefundAmountByDay(ReturnStatus.REFUNDED, range.from(), range.to()).stream()
            .collect(Collectors.toMap(day ->
                    LocalDate.of(day.year(), day.month(), day.day()),
                    Function.identity()
            ));

        return range.start().datesUntil(range.end())
            .map(date ->
                adminDashboardMapper.toSalesPointDto(date, sales.get(date), refunds.get(date))
            )
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminDashboardCategoryUnitsResponse> getSalesByCategory(AdminDashboardPeriodRequest period) {
        AdminDashboardSupport.Period range = AdminDashboardSupport.periodOf(period);

        return orderItemRepository.sumUnitsByCategory(
                OrderStatus.HISTORY_STATUSES, range.from(), range.to()
            )
            .stream()
            .map(adminDashboardMapper::toCategoryUnitsDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminDashboardStatusCountResponse> getOrdersByStatus(AdminDashboardPeriodRequest period) {
        AdminDashboardSupport.Period range = AdminDashboardSupport.periodOf(period);

        Map<OrderStatus, Long> counts = orderRepository
            .countByStatusInPeriod(OrderStatus.HISTORY_STATUSES, range.from(), range.to()).stream()
            .collect(Collectors.toMap(StatusCountProjection::status, StatusCountProjection::count));

        return OrderStatus.HISTORY_STATUSES.stream()
            .map(status ->
                adminDashboardMapper.toStatusCountDto(status, counts.getOrDefault(status, 0L))
            )
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminDashboardTopProductResponse> getTopProducts(AdminDashboardPeriodRequest period) {
        AdminDashboardSupport.Period range = AdminDashboardSupport.periodOf(period);

        List<TopProductProjection> products = orderItemRepository.findTopProducts(
            OrderStatus.HISTORY_STATUSES,
            range.from(),
            range.to(),
            PageRequest.of(0, Constants.DASHBOARD_LIST_LIMIT)
        );

        Map<Long, String> covers = productSkuImageService.findCoversByProductId(
            products.stream().map(TopProductProjection::productId).toList()
        );

        return products.stream()
            .map(product ->
                adminDashboardMapper.toTopProductDto(product, covers.get(product.productId()))
            )
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminDashboardDiscountCodeResponse> getTopDiscountCodes(AdminDashboardPeriodRequest period) {
        AdminDashboardSupport.Period range = AdminDashboardSupport.periodOf(period);

        Pageable limit = PageRequest.of(0, Constants.DASHBOARD_LIST_LIMIT);

        return orderRepository.findTopDiscountCodes(
                OrderStatus.HISTORY_STATUSES, range.from(), range.to(), limit
            )
            .stream()
            .map(adminDashboardMapper::toDiscountCodeDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminDashboardRecentOrderResponse> getRecentOrders() {
        Specification<Order> specification = Specification
            .where(OrderSpecification.hasStatusIn(OrderStatus.HISTORY_STATUSES))
            .and(OrderSpecification.orderByRecentDesc());

        Pageable limit = PageRequest.of(0, Constants.DASHBOARD_LIST_LIMIT);

        return orderRepository.findAll(specification, limit)
            .map(adminDashboardMapper::toRecentOrderDto)
            .getContent();
    }

    private PeriodTotalsResult totals(Instant from, Instant to) {
        BigDecimal revenue = orderRepository.sumTotalAmountInPeriod(OrderStatus.HISTORY_STATUSES, from, to);
        BigDecimal refunds = orderReturnRepository.sumRefundAmountInPeriod(ReturnStatus.REFUNDED, from, to);
        long orders = orderRepository.countInPeriod(OrderStatus.HISTORY_STATUSES, from, to);

        return adminDashboardMapper.toPeriodTotals(revenue, refunds, orders);
    }
}
