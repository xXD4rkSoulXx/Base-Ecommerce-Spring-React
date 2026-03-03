/*
 *  A pasta dto contém todos os requests e responses.
 *
 *  Response é o que o utilizador pede ou pretende receber, por exemplo, todos os dados do User,
 * então todos os campos do User que preciso, em que Password não vem porque não se pretende.
 * */

package com.d4rksoul.base.ecommerce.dto;

import com.d4rksoul.base.ecommerce.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto {
    private Long id;

    private String name;

    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private UserRole role;

    @JsonProperty("address_id")
    private Long addressId;
}
