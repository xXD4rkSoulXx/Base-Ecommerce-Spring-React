package com.aprenderSpring.ecommerceSpring.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRequestDto {
    @NotBlank
    private String name;

    private String description;

    @JsonProperty("image_url")
    private String imageUrl;

    @PositiveOrZero
    private BigDecimal price;

    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("discount_id")
    private Long discountId;
}
