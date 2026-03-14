package com.d4rksoul.base.ecommerce.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PageResponse<T>(
    List<T> content,

    int page,

    int size,

    @JsonProperty("total_elements")
    int totalElements,

    @JsonProperty("total_pages")
    int totalPages,

    @JsonProperty("has_previous")
    boolean hasPrevious,

    @JsonProperty("has_next")
    boolean hasNext,

    @JsonProperty("is_first")
    boolean isFirst,

    @JsonProperty("is_last")
    boolean isLast
) {}
