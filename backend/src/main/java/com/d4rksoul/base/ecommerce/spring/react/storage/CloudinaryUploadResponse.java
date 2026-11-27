package com.d4rksoul.base.ecommerce.spring.react.storage;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CloudinaryUploadResponse(
    @JsonProperty("secure_url")
    String secureUrl
) {}
