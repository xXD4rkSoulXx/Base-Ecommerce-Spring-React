package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProfileResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.UserResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toDto(User user) {
        return UserResponse.builder()
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(user.getRole())
            .permissions(user.getRole().getPermissions())
            .build();
    }

    public ProfileResponse toProfileDto(User user, long totalOrders) {
        return ProfileResponse.builder()
            .user(toDto(user))
            .createdAt(user.getCreatedAt())
            .totalOrders(totalOrders)
            .build();
    }
}
