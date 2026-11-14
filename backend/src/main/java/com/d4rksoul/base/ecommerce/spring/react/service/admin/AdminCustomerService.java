package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.CustomerOrderStatsProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminCustomerOrderSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminCustomerSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.OrderSummaryResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminCustomerResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import com.d4rksoul.base.ecommerce.spring.react.exception.UserNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.OrderMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminCustomerMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import com.d4rksoul.base.ecommerce.spring.react.specification.OrderSpecification;
import com.d4rksoul.base.ecommerce.spring.react.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus.HISTORY_STATUSES;

@Service
@RequiredArgsConstructor
public class AdminCustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AdminCustomerMapper adminCustomerMapper;
    private final OrderMapper orderMapper;
    private final PageMapper pageMapper;

    @Transactional(readOnly = true)
    public PageResponse<AdminCustomerResponse> getCustomers(AdminCustomerSearchRequest filter) {
        Specification<User> specification = Specification
            .where(UserSpecification.hasRole(UserRole.CUSTOMER))
            .and(UserSpecification.containsNameOrEmail(filter.q()))
            .and(UserSpecification.orderByRecentDesc());

        Pageable pageable = PageRequest.of(filter.page(), filter.size());
        Page<User> usersPage = userRepository.findAll(specification, pageable);

        List<Long> userIds = usersPage.getContent().stream()
            .map(User::getId)
            .toList();

        Map<Long, CustomerOrderStatsProjection> customerStats = findStatsByUserId(userIds);

        Page<AdminCustomerResponse> responsePage = usersPage.map(user ->
            adminCustomerMapper.toDto(user, customerStats.get(user.getId()))
        );

        return pageMapper.toDto(responsePage);
    }

    @Transactional(readOnly = true)
    public AdminCustomerResponse getCustomer(Long id) {
        User user = userRepository.findByIdAndRole(id, UserRole.CUSTOMER)
            .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        CustomerOrderStatsProjection stats = findStatsByUserId(List.of(id)).get(id);

        return adminCustomerMapper.toDto(user, stats);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getCustomerOrders(
        Long id, AdminCustomerOrderSearchRequest filter
    ) {
        userRepository.findByIdAndRole(id, UserRole.CUSTOMER)
            .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        Specification<Order> specification = Specification
            .where(OrderSpecification.belongsToUser(id))
            .and(OrderSpecification.hasStatusIn(HISTORY_STATUSES))
            .and(OrderSpecification.orderByRecentDesc());

        Pageable pageable = PageRequest.of(filter.page(), filter.size());
        Page<OrderSummaryResponse> ordersPage = orderRepository.findAll(specification, pageable)
            .map(orderMapper::toSummaryDto);

        return pageMapper.toDto(ordersPage);
    }

    private Map<Long, CustomerOrderStatsProjection> findStatsByUserId(Collection<Long> userIds) {
        return orderRepository.findCustomerStatsByUserIds(userIds, HISTORY_STATUSES).stream()
            .collect(Collectors.toMap(CustomerOrderStatsProjection::userId, Function.identity()));
    }
}
