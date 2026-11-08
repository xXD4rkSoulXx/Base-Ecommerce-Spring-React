package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.DiscountCodeUsageProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminCreateDiscountCodeRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminDiscountCodeSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminUpdateDiscountCodeRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDiscountCodeResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditEntityType;
import com.d4rksoul.base.ecommerce.spring.react.exception.DiscountCodeAlreadyExistsException;
import com.d4rksoul.base.ecommerce.spring.react.exception.DiscountCodeNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminDiscountCodeMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.DiscountCodeRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.specification.DiscountCodeSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDiscountCodeService {

    private final DiscountCodeRepository discountCodeRepository;
    private final OrderRepository orderRepository;
    private final AdminAuditLogService adminAuditLogService;
    private final AdminDiscountCodeMapper adminDiscountCodeMapper;
    private final PageMapper pageMapper;

    @Transactional
    public AdminDiscountCodeResponse createDiscountCode(AdminCreateDiscountCodeRequest request) {
        if (discountCodeRepository.existsByCode(request.code())) {
            throw new DiscountCodeAlreadyExistsException("Discount code " + request.code() + " already exists.");
        }

        DiscountCode discountCode = discountCodeRepository.save(adminDiscountCodeMapper.toEntity(request));

        adminAuditLogService.register(
                AuditAction.CREATED,
                AuditEntityType.DISCOUNT_CODE,
                discountCode.getId(),
                discountCode.getCode(),
                discountCode.getDiscountPercentage() + "%"
        );

        return adminDiscountCodeMapper.toDto(discountCode, 0);
    }

    @Transactional
    public void activateDiscountCode(Long id) {
        DiscountCode discountCode = discountCodeRepository.findById(id)
                .orElseThrow(() -> new DiscountCodeNotFoundException("Discount code not found with id: " + id));

        discountCode.setActive(true);

        adminAuditLogService.register(
                AuditAction.ACTIVATED,
                AuditEntityType.DISCOUNT_CODE,
                id,
                discountCode.getCode()
        );
    }

    @Transactional
    public void deactivateDiscountCode(Long id) {
        DiscountCode discountCode = discountCodeRepository.findById(id)
                .orElseThrow(() -> new DiscountCodeNotFoundException("Discount code not found with id: " + id));

        discountCode.setActive(false);

        adminAuditLogService.register(AuditAction.DEACTIVATED, AuditEntityType.DISCOUNT_CODE, id, discountCode.getCode());
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminDiscountCodeResponse> getDiscountCodes(AdminDiscountCodeSearchRequest filter) {
        Specification<DiscountCode> specification = Specification
                .where(DiscountCodeSpecification.containsCode(filter.q()))
                .and(DiscountCodeSpecification.hasActive(filter.active()))
                .and(DiscountCodeSpecification.orderByRecentDesc());

        Pageable pageable = PageRequest.of(filter.page(), filter.size());
        Page<DiscountCode> discountCodesPage = discountCodeRepository.findAll(specification, pageable);

        List<Long> discountCodeIds = discountCodesPage.getContent().stream()
                .map(DiscountCode::getId)
                .toList();

        Map<Long, Long> orders = orderRepository.countOrdersByDiscountCodeIds(discountCodeIds).stream()
                .collect(Collectors.toMap(
                        DiscountCodeUsageProjection::discountCodeId,
                        DiscountCodeUsageProjection::orders
                ));

        Page<AdminDiscountCodeResponse> responsePage = discountCodesPage.map(discountCode ->
                adminDiscountCodeMapper.toDto(discountCode, orders.getOrDefault(discountCode.getId(), 0L))
        );

        return pageMapper.toDto(responsePage);
    }

    @Transactional
    public AdminDiscountCodeResponse updateDiscountCode(Long id, AdminUpdateDiscountCodeRequest request) {
        DiscountCode discountCode = discountCodeRepository.findById(id)
                .orElseThrow(() -> new DiscountCodeNotFoundException("Discount code not found with id: " + id));

        String previousPercentage = discountCode.getDiscountPercentage() + "%";

        adminDiscountCodeMapper.updateEntity(discountCode, request);

        adminAuditLogService.register(
                AuditAction.UPDATED,
                AuditEntityType.DISCOUNT_CODE,
                id,
                discountCode.getCode(),
                previousPercentage + " → " + discountCode.getDiscountPercentage() + "%"
        );

        return adminDiscountCodeMapper.toDto(discountCode, orderRepository.countByDiscountCodeId(id));
    }
}
