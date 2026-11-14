package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MergeCartRequest(
    @Size(max = 20, message = "Too many items.")
    List<@NotNull(message = "Items cannot be null.") @Valid MergeCartItemRequest> items
) {}
