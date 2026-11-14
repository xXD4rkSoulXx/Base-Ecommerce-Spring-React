package com.d4rksoul.base.ecommerce.spring.react.dto.result;

import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import lombok.Builder;

@Builder
public record RotateRefreshTokenResult(
    User user,

    String rawRefreshToken
) {}
