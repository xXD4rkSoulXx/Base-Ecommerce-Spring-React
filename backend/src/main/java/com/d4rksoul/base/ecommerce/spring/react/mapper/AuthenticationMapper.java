package com.d4rksoul.base.ecommerce.spring.react.mapper;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.RegisterRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.UserResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.LoginResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationMapper {

    public User toEntity(RegisterRequest request, String encodedPassword) {
        return User.builder()
            .email(request.email())
            .password(encodedPassword)
            .firstName(request.firstName())
            .lastName(request.lastName())
            .role(UserRole.CUSTOMER)
            .build();
    }

    public LoginResult toLoginResult(UserResponse user, String accessToken, String refreshToken) {
        return LoginResult.builder()
            .user(user)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }
}
