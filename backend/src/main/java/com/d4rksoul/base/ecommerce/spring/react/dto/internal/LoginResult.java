package com.d4rksoul.base.ecommerce.spring.react.dto.internal;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.UserResponse;

public record LoginResult(

        UserResponse user,

        String accessToken,

        String refreshToken
) {}
