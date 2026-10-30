package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProfileResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.TeamMemberResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.UserResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public TeamMemberResponse toTeamMemberDto(User user) {
        return TeamMemberResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public List<TeamMemberResponse> toTeamMemberDtos(List<User> users) {
        return users.stream()
                .map(this::toTeamMemberDto)
                .toList();
    }
}
