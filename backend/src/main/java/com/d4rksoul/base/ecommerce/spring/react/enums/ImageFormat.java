package com.d4rksoul.base.ecommerce.spring.react.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageFormat {
    JPEG("jpg"),
    PNG("png"),
    WEBP("webp");

    private final String extension;
}
