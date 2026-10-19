package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.RegisterRequest;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationMapper {

    public User toEntity(RegisterRequest request) {
        return User.builder()
                .email(request.email())
                .password(request.password())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .build();
    }
}
