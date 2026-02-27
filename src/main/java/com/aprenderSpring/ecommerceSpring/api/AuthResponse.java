/*
 *  A pasta api é responsável por todos os Responses.
 *
 *  Os Requests constam na pasta dto.
 *
 *
 *  O Auth Response é responsável por receber o estado, o token e as informações
 * relacionadas ao Login efetuado.
 * */

package com.aprenderSpring.ecommerceSpring.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private boolean success;

    private String message;

    private String token;

    private String role;

    @JsonProperty("expiration_time")
    private Instant expirationTime;

    private final Instant timestamp = Instant.now();
}
