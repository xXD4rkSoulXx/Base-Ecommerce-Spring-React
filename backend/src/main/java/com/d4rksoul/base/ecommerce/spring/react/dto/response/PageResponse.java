package com.d4rksoul.base.ecommerce.spring.react.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record PageResponse<T>(

        List<T> content,

        int page,

        int size,

        long totalElements,

        int totalPages,

        boolean first,

        boolean last
) {}
