package com.aprenderSpring.ecommerceSpring.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscountRequestDto {
    @NotBlank
    @Min(0)
    @Min(100)
    private int percentage;

    @NotBlank
    @JsonProperty("start_date")
    private LocalDate startDate;

    @NotBlank
    @JsonProperty("end_date")
    private LocalDate endDate;
}
