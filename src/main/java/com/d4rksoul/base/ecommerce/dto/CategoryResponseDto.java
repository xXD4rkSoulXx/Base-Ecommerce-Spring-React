/*
 *  A pasta dto contém todos os requests e responses.
 *
 *  Response é o que o utilizador pede ou pretende receber, por exemplo, todos os dados do User,
 * então todos os campos do User que preciso, em que Password não vem porque não se pretende.
 * */

package com.d4rksoul.base.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponseDto {
    private Long id;

    private String name;
}
