package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminReturnSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminReturnResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturnItem;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditEntityType;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.ReturnNotAllowedException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ReturnNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminReturnMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderReturnRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.OrderReturnApprovalService;
import com.d4rksoul.base.ecommerce.spring.react.specification.ReturnSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReturnService {

    private final OrderReturnRepository orderReturnRepository;
    private final OrderReturnItemRepository orderReturnItemRepository;
    private final OrderReturnApprovalService orderReturnApprovalService;
    private final AdminAuditLogService adminAuditLogService;
    private final AdminReturnMapper adminReturnMapper;
    private final PageMapper pageMapper;

    @Transactional
    public AdminReturnResponse approve(Long id) {
        OrderReturn orderReturn = orderReturnApprovalService.approve(id)
                .orElseThrow(() -> unavailable(id));

        return complete(orderReturn, AuditAction.APPROVED, null);
    }

    @Transactional
    public AdminReturnResponse reject(Long id, String reason) {
        String trimmedReason = reason.trim();

        OrderReturn orderReturn = orderReturnApprovalService.reject(id, trimmedReason)
                .orElseThrow(() -> unavailable(id));

        return complete(orderReturn, AuditAction.REJECTED, trimmedReason);
    }

    @Transactional
    public AdminReturnResponse receive(Long id) {
        OrderReturn orderReturn = orderReturnApprovalService.receive(id)
                .orElseThrow(() -> unavailable(id));

        return complete(orderReturn, AuditAction.RECEIVED, null);
    }

    @Transactional
    public AdminReturnResponse refund(Long id) {
        OrderReturn orderReturn = orderReturnApprovalService.refund(id)
                .orElseThrow(() -> unavailable(id));

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

        List<OrderReturnItem> items = orderReturnItemRepository
                .findByOrderReturnInOrderByIdAsc(returnsPage.getContent());

        Page<AdminReturnResponse> responsePage = new PageImpl<>(
                adminReturnMapper.toDtos(returnsPage.getContent(), items),
                pageable,
                returnsPage.getTotalElements()
        );

        return pageMapper.toDto(responsePage);
    }

    private AdminReturnResponse complete(OrderReturn orderReturn, AuditAction action, String details) {
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
}
