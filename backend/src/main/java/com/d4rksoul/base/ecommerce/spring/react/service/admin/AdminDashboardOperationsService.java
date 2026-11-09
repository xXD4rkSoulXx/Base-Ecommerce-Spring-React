package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardCountResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardLowStockResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardReturnResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminDashboardMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.specification.ProductSkuSpecification;
import com.d4rksoul.base.ecommerce.spring.react.specification.ReturnSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardOperationsService {

    private final OrderRepository orderRepository;
    private final OrderReturnRepository orderReturnRepository;
    private final ProductSkuRepository productSkuRepository;
    private final AdminDashboardMapper adminDashboardMapper;

    @Transactional(readOnly = true)
    public AdminDashboardCountResponse getPendingOrders() {
        return adminDashboardMapper.toCountDto(
                orderRepository.countByStatusIn(OrderStatus.FULFILLMENT_STATUSES)
        );
    }

    @Transactional(readOnly = true)
    public AdminDashboardCountResponse getPendingReturns() {
        return adminDashboardMapper.toCountDto(
                orderReturnRepository.countByStatusIn(ReturnStatus.PENDING_STATUSES)
        );
    }

    @Transactional(readOnly = true)
    public AdminDashboardCountResponse getLowStock() {
        return adminDashboardMapper.toCountDto(productSkuRepository.count(
                Specification
                        .where(ProductSkuSpecification.isActive())
                        .and(ProductSkuSpecification.hasLowStock(
                                true,
                                DashboardSupport.LOW_STOCK_THRESHOLD
                        ))
        ));
    }

    @Transactional(readOnly = true)
    public List<AdminDashboardReturnResponse> getReturnsToHandle() {
        Specification<OrderReturn> specification = Specification
                .where(ReturnSpecification.hasStatusIn(ReturnStatus.PENDING_STATUSES))
                .and(ReturnSpecification.orderByPendingPriority());

        return orderReturnRepository.findAll(specification, PageRequest.of(0, DashboardSupport.LIST_LIMIT))
                .getContent().stream()
                .map(adminDashboardMapper::toReturnDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminDashboardLowStockResponse> getLowestStock() {
        Specification<ProductSku> specification = Specification
                .where(ProductSkuSpecification.isActive())
                .and(ProductSkuSpecification.hasLowStock(true, DashboardSupport.LOW_STOCK_THRESHOLD))
                .and(ProductSkuSpecification.orderByStockAsc());

        return productSkuRepository.findAll(specification, PageRequest.of(0, DashboardSupport.LIST_LIMIT))
                .getContent().stream()
                .map(adminDashboardMapper::toLowStockDto)
                .toList();
    }
}
