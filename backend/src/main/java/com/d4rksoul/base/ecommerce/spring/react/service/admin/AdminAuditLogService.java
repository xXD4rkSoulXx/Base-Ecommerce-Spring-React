package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminAuditLogSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminAuditLogResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.AuditLog;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditEntityType;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminAuditLogMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.AuditLogRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.UserService;
import com.d4rksoul.base.ecommerce.spring.react.specification.AuditLogSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserService userService;
    private final AdminAuditLogMapper adminAuditLogMapper;
    private final PageMapper pageMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void register(AuditAction action, AuditEntityType entityType, Long entityId, String entityLabel) {
        persist(action, entityType, entityId, entityLabel, null);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void register(
        AuditAction action, AuditEntityType entityType, Long entityId, String entityLabel, String details
    ) {
        persist(action, entityType, entityId, entityLabel, details);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registerVariant(AuditAction action, ProductSku sku, String details) {
        persist(
            action,
            AuditEntityType.VARIANT,
            sku.getId(),
            sku.getProduct().getRef() + " · " + sku.getSize().getLabel() + " " + sku.getColor(),
            details
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminAuditLogResponse> getAuditLogs(AdminAuditLogSearchRequest filter) {
        Specification<AuditLog> specification = Specification
            .where(AuditLogSpecification.containsQuery(filter.q()))
            .and(AuditLogSpecification.hasEntityType(filter.entityType()))
            .and(AuditLogSpecification.hasAction(filter.action()))
            .and(AuditLogSpecification.orderByRecentDesc());

        Pageable pageable = PageRequest.of(filter.page(), filter.size());
        Page<AdminAuditLogResponse> responsePage = auditLogRepository.findAll(specification, pageable)
            .map(adminAuditLogMapper::toDto);

        return pageMapper.toDto(responsePage);
    }

    private void persist(
        AuditAction action,
        AuditEntityType entityType,
        Long entityId,
        String entityLabel,
        String details
    ) {
        String truncatedDetails = details != null && details.length() > 500
            ? details.substring(0, 500)
            : details;

        String adminEmail = userService.getCurrentUser().getEmail();

        AuditLog auditLog = adminAuditLogMapper.toEntity(
            adminEmail, action, entityType, entityId, entityLabel, truncatedDetails
        );

        auditLogRepository.save(auditLog);
    }
}
