package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminDashboardPeriodRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminAuditLogResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardCustomersResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDashboardReviewsResponse;
import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminAuditLogMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminDashboardMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.AuditLogRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ReviewRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import com.d4rksoul.base.ecommerce.spring.react.specification.AuditLogSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardCommunityService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final AuditLogRepository auditLogRepository;
    private final AdminDashboardMapper adminDashboardMapper;
    private final AdminAuditLogMapper adminAuditLogMapper;

    @Transactional(readOnly = true)
    public AdminDashboardCustomersResponse getCustomers(AdminDashboardPeriodRequest period) {
        LocalDate start = DashboardSupport.startOf(period);
        Instant from = DashboardSupport.toInstant(start);
        Instant to = DashboardSupport.toInstant(DashboardSupport.endOf());
        Instant previousFrom = DashboardSupport.toInstant(start.minusDays(period.days()));

        return adminDashboardMapper.toCustomersDto(
                userRepository.countByRoleInPeriod(UserRole.CUSTOMER, from, to),
                userRepository.countByRoleInPeriod(UserRole.CUSTOMER, previousFrom, from),
                userRepository.countByRole(UserRole.CUSTOMER)
        );
    }

    @Transactional(readOnly = true)
    public AdminDashboardReviewsResponse getReviews(AdminDashboardPeriodRequest period) {
        LocalDate start = DashboardSupport.startOf(period);
        Instant from = DashboardSupport.toInstant(start);
        Instant to = DashboardSupport.toInstant(DashboardSupport.endOf());
        Instant previousFrom = DashboardSupport.toInstant(start.minusDays(period.days()));

        return adminDashboardMapper.toReviewsDto(
                reviewRepository.countInPeriod(from, to),
                reviewRepository.countInPeriod(previousFrom, from),
                reviewRepository.averageRatingInPeriod(from, to),
                reviewRepository.averageRatingInPeriod(previousFrom, from)
        );
    }

    @Transactional(readOnly = true)
    public List<AdminAuditLogResponse> getRecentActivity() {
        return auditLogRepository.findAll(
                        AuditLogSpecification.orderByRecentDesc(),
                        PageRequest.of(0, DashboardSupport.LIST_LIMIT)
                ).getContent().stream()
                .map(adminAuditLogMapper::toDto)
                .toList();
    }
}
