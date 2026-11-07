package com.d4rksoul.base.ecommerce.spring.react.mapper.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.CustomerOrderStatsProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminCustomerResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AdminCustomerMapper {

    public AdminCustomerResponse toDto(User user, CustomerOrderStatsProjection stats) {
        return AdminCustomerResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .orders(stats != null ? stats.orders() : 0)
                .lastOrderAt(stats != null ? stats.lastOrderAt() : null)
                .build();
    }
}
