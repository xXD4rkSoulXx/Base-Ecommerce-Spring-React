package com.d4rksoul.base.ecommerce.spring.react.service.admin;

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
import com.d4rksoul.base.ecommerce.spring.react.service.ProductImageService;
import com.d4rksoul.base.ecommerce.spring.react.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ProductImageService productImageService;
    private final AdminDashboardMapper adminDashboardMapper;

    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getSummary(AdminDashboardPeriodRequest period) {
        LocalDate start = DashboardSupport.startOf(period);
        LocalDate end = DashboardSupport.endOf();

        return adminDashboardMapper.toSummaryDto(
                totals(start, end),
                totals(start.minusDays(period.days()), start)
        );
    }

    @Transactional(readOnly = true)
    public List<AdminDashboardSalesPointResponse> getSales(AdminDashboardPeriodRequest period) {
        LocalDate start = DashboardSupport.startOf(period);
        LocalDate end = DashboardSupport.endOf();
        Instant from = DashboardSupport.toInstant(start);
        Instant to = DashboardSupport.toInstant(end);

        Map<LocalDate, DailyTotalProjection> sales = orderRepository
                .sumTotalAmountByDay(OrderStatus.HISTORY_STATUSES, from, to).stream()
                .collect(Collectors.toMap(this::dateOf, Function.identity()));

        Map<LocalDate, DailyTotalProjection> refunds = orderReturnRepository
                .sumRefundAmountByDay(ReturnStatus.REFUNDED, from, to).stream()
                .collect(Collectors.toMap(this::dateOf, Function.identity()));

        return start.datesUntil(end)
                .map(date -> adminDashboardMapper.toSalesPointDto(date, sales.get(date), refunds.get(date)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminDashboardStatusCountResponse> getOrdersByStatus(AdminDashboardPeriodRequest period) {
        Instant from = DashboardSupport.toInstant(DashboardSupport.startOf(period));
        Instant to = DashboardSupport.toInstant(DashboardSupport.endOf());

        Map<OrderStatus, Long> counts = orderRepository
                .countByStatusInPeriod(OrderStatus.HISTORY_STATUSES, from, to).stream()
                .collect(Collectors.toMap(StatusCountProjection::status, StatusCountProjection::count));

        return OrderStatus.HISTORY_STATUSES.stream()
                .map(status -> adminDashboardMapper.toStatusCountDto(status, counts.getOrDefault(status, 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminDashboardCategoryUnitsResponse> getSalesByCategory(AdminDashboardPeriodRequest period) {
        Instant from = DashboardSupport.toInstant(DashboardSupport.startOf(period));
        Instant to = DashboardSupport.toInstant(DashboardSupport.endOf());

        return orderItemRepository.sumUnitsByCategory(OrderStatus.HISTORY_STATUSES, from, to).stream()
                .map(adminDashboardMapper::toCategoryUnitsDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminDashboardTopProductResponse> getTopProducts(AdminDashboardPeriodRequest period) {
        Instant from = DashboardSupport.toInstant(DashboardSupport.startOf(period));
        Instant to = DashboardSupport.toInstant(DashboardSupport.endOf());

        List<TopProductProjection> products = orderItemRepository.findTopProducts(
                OrderStatus.HISTORY_STATUSES,
                from,
                to,
                PageRequest.of(0, DashboardSupport.LIST_LIMIT)
        );

        Map<Long, String> covers = productImageService.findCoversByProductId(
                products.stream().map(TopProductProjection::productId).toList()
        );

        return products.stream()
                .map(product -> adminDashboardMapper.toTopProductDto(product, covers.get(product.productId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminDashboardDiscountCodeResponse> getTopDiscountCodes(AdminDashboardPeriodRequest period) {
        Instant from = DashboardSupport.toInstant(DashboardSupport.startOf(period));
        Instant to = DashboardSupport.toInstant(DashboardSupport.endOf());

        return orderRepository.findTopDiscountCodes(
                        OrderStatus.HISTORY_STATUSES,
                        from,
                        to,
                        PageRequest.of(0, DashboardSupport.LIST_LIMIT)
                ).stream()
                .map(adminDashboardMapper::toDiscountCodeDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminDashboardRecentOrderResponse> getRecentOrders() {
        Specification<Order> specification = Specification
                .where(OrderSpecification.hasStatusIn(OrderStatus.HISTORY_STATUSES))
                .and(OrderSpecification.orderByRecentDesc());

        return orderRepository.findAll(specification, PageRequest.of(0, DashboardSupport.LIST_LIMIT))
                .getContent().stream()
                .map(adminDashboardMapper::toRecentOrderDto)
                .toList();
    }

    private PeriodTotalsResult totals(LocalDate start, LocalDate end) {
        Instant from = DashboardSupport.toInstant(start);
        Instant to = DashboardSupport.toInstant(end);

        return new PeriodTotalsResult(
                orderRepository.sumTotalAmountInPeriod(OrderStatus.HISTORY_STATUSES, from, to),
                orderReturnRepository.sumRefundAmountInPeriod(ReturnStatus.REFUNDED, from, to),
                orderRepository.countInPeriod(OrderStatus.HISTORY_STATUSES, from, to)
        );
    }

    private LocalDate dateOf(DailyTotalProjection projection) {
        return LocalDate.of(projection.year(), projection.month(), projection.day());
    }
}
