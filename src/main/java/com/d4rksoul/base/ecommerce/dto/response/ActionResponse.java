/*
 *  A pasta dto contém todos os requests e responses.
 *
 *
 *  O Action Response é responsável por receber o estado de todas as ações efetuadas.
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
public class ActionResponse {
    private boolean success;

    private String message;

    private String action;

    @JsonProperty("resource_id")
    private Long resourceId;

    private final Instant timestamp = Instant.now();
}
