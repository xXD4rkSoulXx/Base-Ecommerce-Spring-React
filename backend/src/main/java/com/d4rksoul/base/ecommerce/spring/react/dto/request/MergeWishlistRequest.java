package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MergeWishlistRequest(
    @Size(max = 100, message = "Too many items.")
    List<@NotNull(message = "Items cannot be null.") Long> productIds
) {}
