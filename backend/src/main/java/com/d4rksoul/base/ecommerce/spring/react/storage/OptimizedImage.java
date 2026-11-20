package com.d4rksoul.base.ecommerce.spring.react.storage;

import com.d4rksoul.base.ecommerce.spring.react.enums.ImageFormat;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Objects;

public record OptimizedImage(
    byte[] content,

    ImageFormat format
) {

    @Override
    public boolean equals(Object other) {
        return other instanceof OptimizedImage(byte[] otherContent, ImageFormat otherFormat)
            && Arrays.equals(content, otherContent)
            && format == otherFormat;
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(content) + Objects.hashCode(format);
    }

    @Override
    @NonNull
    public String toString() {
        return "OptimizedImage[content=" + content.length + " bytes, format=" + format + "]";
    }
}
