/*
 *  A pasta dto contém todos os requests e responses.
 *
 *
 *  O Auth Response é responsável por receber o estado, o token e as informações
 * relacionadas ao Login efetuado.
 * */

package com.d4rksoul.base.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
