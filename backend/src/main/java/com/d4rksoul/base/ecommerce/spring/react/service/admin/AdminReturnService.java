package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ReturnedQuantityProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminReturnSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminReturnResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.StripeRefundResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturnItem;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditEntityType;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.RefundFailedException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ReturnNotAllowedException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ReturnNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.StripeApiException;
import com.d4rksoul.base.ecommerce.spring.react.integration.stripe.StripeApiClient;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminReturnMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderStatusEventService;
import com.d4rksoul.base.ecommerce.spring.react.specification.ReturnSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReturnService {

    private static final Set<String> FAILED_REFUND_STATUSES = Set.of("failed", "canceled");

    private final OrderReturnRepository orderReturnRepository;
    private final OrderReturnItemRepository orderReturnItemRepository;
    private final ProductSkuRepository productSkuRepository;
    private final OrderStatusEventService orderStatusEventService;
    private final AdminAuditLogService adminAuditLogService;
    private final AdminReturnMapper adminReturnMapper;
    private final PageMapper pageMapper;
    private final StripeApiClient stripeApiClient;

    @Transactional
    public AdminReturnResponse approve(Long id) {
        OrderReturn orderReturn = orderReturnRepository.findByIdAndStatus(id, ReturnStatus.REQUESTED)
            .orElseThrow(() -> unavailable(id));

        orderReturn.setStatus(ReturnStatus.APPROVED);
        orderReturn.setApprovedAt(Instant.now());

        return complete(orderReturn, AuditAction.APPROVED, null);
    }

    @Transactional
    public AdminReturnResponse reject(Long id, String reason) {
        OrderReturn orderReturn = orderReturnRepository.findByIdAndStatus(id, ReturnStatus.REQUESTED)
            .orElseThrow(() -> unavailable(id));

        orderReturn.setStatus(ReturnStatus.REJECTED);
        orderReturn.setRejectionReason(reason.trim());
        orderReturn.setRejectedAt(Instant.now());

        orderStatusEventService.transition(
            orderReturn.getOrder().getId(), OrderStatus.RETURN_REQUESTED, OrderStatus.DELIVERED
        );

        return complete(orderReturn, AuditAction.REJECTED, reason.trim());
    }

    @Transactional
    public AdminReturnResponse receive(Long id) {
        OrderReturn orderReturn = orderReturnRepository.findByIdAndStatus(id, ReturnStatus.APPROVED)
            .orElseThrow(() -> unavailable(id));

        orderReturn.setStatus(ReturnStatus.RECEIVED);
        orderReturn.setReceivedAt(Instant.now());

        List<OrderReturnItem> returnItems = orderReturnItemRepository.findByOrderReturnId(id);
        for (OrderReturnItem item : returnItems) {
            productSkuRepository.incrementStock(
                item.getOrderItem().getProductSku().getId(), item.getQuantity()
            );
        }

        return complete(orderReturn, AuditAction.RECEIVED, null);
    }

    @Transactional
    public AdminReturnResponse refund(Long id) {
        OrderReturn orderReturn = orderReturnRepository.findByIdAndStatus(id, ReturnStatus.RECEIVED)
            .orElseThrow(() -> unavailable(id));

        List<OrderReturnItem> returnItems = orderReturnItemRepository.findByOrderReturnId(id);

        Map<Long, Long> returnedByItemId = orderReturnItemRepository
            .sumReturnedQuantities(
                orderReturn.getOrder().getId(), ReturnStatus.INACTIVE_STATUSES
            )
            .stream()
            .collect(Collectors.toMap(
                ReturnedQuantityProjection::orderItemId, ReturnedQuantityProjection::quantity
            ));

        boolean fullyReturned = orderReturn.getOrder().getOrderItems().stream()
            .allMatch(item ->
                returnedByItemId.getOrDefault(item.getId(), 0L) >= item.getQuantity()
            );

        BigDecimal remaining = orderReturn.getOrder().getTotalAmount().subtract(
            orderReturnRepository.sumRefundAmountByOrderIdAndStatus(
                orderReturn.getOrder().getId(), ReturnStatus.REFUNDED
            )
        );

        BigDecimal amount = calculateRefund(orderReturn.getOrder(), returnItems, remaining, fullyReturned);

        StripeRefundResult refund;
        try {
            refund = stripeApiClient.createRefund(orderReturn.getOrder(), orderReturn, amount);
        } catch (StripeApiException _) {
            throw new RefundFailedException("The refund could not be completed. Please try again.");
        }

        if (FAILED_REFUND_STATUSES.contains(refund.status())) {
            throw new RefundFailedException("The refund could not be completed. Please try again.");
        }

        orderReturn.setStatus(ReturnStatus.REFUNDED);
        orderReturn.setRefundAmount(amount);
        orderReturn.setStripeRefundId(refund.id());
        orderReturn.setRefundedAt(Instant.now());

        orderStatusEventService.transition(
            orderReturn.getOrder().getId(),
            OrderStatus.RETURN_REQUESTED,
            fullyReturned ? OrderStatus.REFUNDED : OrderStatus.DELIVERED
        );

        return complete(orderReturn, AuditAction.REFUNDED, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminReturnResponse> getReturns(AdminReturnSearchRequest filter) {
        Specification<OrderReturn> specification = Boolean.TRUE.equals(filter.pending())
            ? Specification.where(ReturnSpecification.hasStatusIn(ReturnStatus.PENDING_STATUSES))
            .and(ReturnSpecification.orderByPendingPriority())
            : Specification.where(ReturnSpecification.hasStatus(filter.status()))
            .and(ReturnSpecification.orderByRecentDesc());

        Pageable pageable = PageRequest.of(filter.page(), filter.size());

        Page<OrderReturn> returnsPage = orderReturnRepository.findAll(specification, pageable);

        Map<Long, List<OrderReturnItem>> itemsByReturnId = orderReturnItemRepository
            .findByOrderReturnInOrderByIdAsc(returnsPage.getContent())
            .stream()
            .collect(Collectors.groupingBy(item -> item.getOrderReturn().getId()));

        return pageMapper.toDto(returnsPage.map(orderReturn ->
            adminReturnMapper.toDto(orderReturn, itemsByReturnId.getOrDefault(orderReturn.getId(), List.of()))
        ));
    }

    private AdminReturnResponse complete(
        OrderReturn orderReturn, AuditAction action, String details
    ) {
        orderReturnRepository.markManuallyManaged(orderReturn.getId());

        adminAuditLogService.register(
            action,
            AuditEntityType.RETURN,
            orderReturn.getId(),
            "#" + orderReturn.getId(),
            details
        );

        List<OrderReturnItem> items = orderReturnItemRepository.findByOrderReturnId(orderReturn.getId());

        return adminReturnMapper.toDto(orderReturn, items);
    }

    private RuntimeException unavailable(Long id) {
        return orderReturnRepository.existsById(id)
            ? new ReturnNotAllowedException("This return cannot be changed from its current status.")
            : new ReturnNotFoundException("Return not found with id: " + id);
    }

    public BigDecimal calculateRefund(
        Order order, List<OrderReturnItem> returnItems, BigDecimal remaining, boolean fullyReturned
    ) {
        if (fullyReturned) {
            return remaining;
        }
        BigDecimal discountFactor = BigDecimal.valueOf(100L - order.getDiscountPercentage())
            .movePointLeft(2);

        BigDecimal itemsNet = order.getOrderItems().stream()
            .map(item ->
                item.getPrice()
                    .multiply(BigDecimal.valueOf(100L - item.getDiscountPercentage()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
            )
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .multiply(discountFactor);

        BigDecimal returnedNet = returnItems.stream()
            .map(item ->
                item.getOrderItem().getPrice()
                    .multiply(BigDecimal.valueOf(100L - item.getOrderItem().getDiscountPercentage()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
            )
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .multiply(discountFactor);

        double totalWeight = order.getOrderItems().stream()
            .mapToDouble(item -> item.getWeightGrams() * item.getQuantity())
            .sum();

        double returnedWeight = returnItems.stream()
            .mapToDouble(item ->
                item.getOrderItem().getWeightGrams() * item.getQuantity()
            )
            .sum();

        BigDecimal shippingShare = order.getShippingCost()
            .multiply(BigDecimal.valueOf(returnedWeight))
            .divide(BigDecimal.valueOf(totalWeight), 2, RoundingMode.HALF_UP);

        BigDecimal denominator = itemsNet.add(order.getShippingCost());

        if (denominator.signum() == 0) {
            return BigDecimal.ZERO;
        }

        return returnedNet.add(shippingShare)
            .multiply(order.getTotalAmount())
            .divide(denominator, 2, RoundingMode.HALF_UP)
            .min(remaining);
    }
}
