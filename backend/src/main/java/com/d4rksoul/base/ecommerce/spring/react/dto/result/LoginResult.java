package com.d4rksoul.base.ecommerce.spring.react.dto.result;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.UserResponse;
import lombok.Builder;

@Builder
public record LoginResult(
    UserResponse user,

    String accessToken,

    String refreshToken
) {}
