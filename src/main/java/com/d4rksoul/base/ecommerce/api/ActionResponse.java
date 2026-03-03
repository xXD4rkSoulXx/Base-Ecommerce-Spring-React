/*
 *  A pasta api é responsável por todos os Responses vindo do servidor.
 *
 *  Os Requests e os Responses dos dados de cada entidade constam na pasta dto.
 *
 *
 *  O Action Response é responsável por receber o estado de todas as ações efetuadas.
 * */

package com.d4rksoul.base.ecommerce.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionResponse {
    private boolean success;

    private String message;

    private String action;

    @JsonProperty("resource_id")
    private Long resourceId;

    private final Instant timestamp = Instant.now();
}
