package com.d4rksoul.base.ecommerce.spring.react.dto.result;

import com.d4rksoul.base.ecommerce.spring.react.entity.User;

public record RotateRefreshTokenResult(
        User user,
        String newRefreshToken
) {}
