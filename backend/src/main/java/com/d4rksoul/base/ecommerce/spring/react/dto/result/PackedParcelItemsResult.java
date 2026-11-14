package com.d4rksoul.base.ecommerce.spring.react.dto.result;

import lombok.Builder;

import java.util.List;

@Builder
public record PackedParcelItemsResult(
    PackedParcelResult parcel,

    List<PackingItemResult> items
) {}
