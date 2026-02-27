/*
 *  A pasta api é responsável por todos os Responses.
 *
 *  Os Requests constam na pasta dto.
 *
 *
 *  O Action Response é responsável por receber o estado de todas as ações efetuadas.
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
public class ActionResponse {
    private boolean success;

    private String message;

    private String action;

    @JsonProperty("resource_id")
    private Long resourceId;

    private final Instant timestamp = Instant.now();
}
