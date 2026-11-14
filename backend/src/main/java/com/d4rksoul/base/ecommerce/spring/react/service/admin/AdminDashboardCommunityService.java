package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        AdminDashboardSupport.Period range = AdminDashboardSupport.periodOf(period);

        long newCustomers = userRepository.countByRoleInPeriod(UserRole.CUSTOMER, range.from(), range.to());
        long previousNewCustomers = userRepository
            .countByRoleInPeriod(UserRole.CUSTOMER, range.previousFrom(), range.from());
        long totalCustomers = userRepository.countByRole(UserRole.CUSTOMER);

        return adminDashboardMapper.toCustomersDto(newCustomers, previousNewCustomers, totalCustomers);
    }

    @Transactional(readOnly = true)
    public AdminDashboardReviewsResponse getReviews(AdminDashboardPeriodRequest period) {
        AdminDashboardSupport.Period range = AdminDashboardSupport.periodOf(period);

        long reviewCount = reviewRepository.countInPeriod(range.from(), range.to());
        long previousReviewCount = reviewRepository.countInPeriod(range.previousFrom(), range.from());
        Double averageRating = reviewRepository.averageRatingInPeriod(range.from(), range.to());
        Double previousAverageRating = reviewRepository
            .averageRatingInPeriod(range.previousFrom(), range.from());

        return adminDashboardMapper.toReviewsDto(
            reviewCount, previousReviewCount, averageRating, previousAverageRating
        );
    }

    @Transactional(readOnly = true)
    public List<AdminAuditLogResponse> getRecentActivity() {
        Pageable limit = PageRequest.of(0, Constants.DASHBOARD_LIST_LIMIT);

        return auditLogRepository.findAll(AuditLogSpecification.orderByRecentDesc(), limit)
            .map(adminAuditLogMapper::toDto)
            .getContent();
    }
}
