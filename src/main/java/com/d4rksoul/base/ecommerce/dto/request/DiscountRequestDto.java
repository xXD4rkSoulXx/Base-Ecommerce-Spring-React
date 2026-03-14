package com.d4rksoul.base.ecommerce.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record DiscountRequestDto(
    @NotBlank
    @Min(0)
    @Min(100)
    int percentage,

    @NotBlank
    @JsonProperty("start_date")
    LocalDate startDate,

    @NotBlank
    @JsonProperty("end_date")
    LocalDate endDate
) {}
