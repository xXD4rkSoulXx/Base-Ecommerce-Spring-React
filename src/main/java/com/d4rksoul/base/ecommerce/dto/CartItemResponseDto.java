/*
 *  A pasta dto contém todos os requests e responses.
 *
 *  Response é o que o utilizador pede ou pretende receber, por exemplo, todos os dados do User,
 * então todos os campos do User que preciso, em que Password não vem porque não se pretende.
 * */

package com.d4rksoul.base.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemResponseDto {
    private Long id;

    private int quantity;

    @JsonProperty("price_at_moment")
    private BigDecimal priceAtMoment;

    private int discount;

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("cart_id")
    private Long cartId;
}
