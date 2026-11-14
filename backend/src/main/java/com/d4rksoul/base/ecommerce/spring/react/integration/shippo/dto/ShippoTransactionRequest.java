package com.d4rksoul.base.ecommerce.spring.react.integration.shippo.dto;

import lombok.Builder;

@Builder
public record ShippoTransactionRequest(
    String rate,

    boolean async
) {}
