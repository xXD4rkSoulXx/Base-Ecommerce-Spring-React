package com.d4rksoul.base.ecommerce.spring.react.mapper.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminTeamMemberResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AdminTeamMapper {

    public AdminTeamMemberResponse toDto(User user) {
        return AdminTeamMemberResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(user.getRole())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
