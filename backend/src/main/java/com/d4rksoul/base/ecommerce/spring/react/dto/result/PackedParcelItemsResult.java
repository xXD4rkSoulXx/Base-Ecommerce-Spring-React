package com.d4rksoul.base.ecommerce.spring.react.dto.result;

import java.util.List;

public record PackedParcelItemsResult(
        PackedParcelResult parcel,

        List<PackingItemResult> items
) {}
