/*
 *  A pasta api é responsável por todos os Responses vindo do servidor.
 *
 *  Os Requests e os Responses dos dados de cada entidade constam na pasta dto.
 *
 *
 *  O Api Error é responsável por receber os detalhes dos erros que ocorrem.
 * */

package com.d4rksoul.base.ecommerce.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private int status;

    private String error;

    private String message;

    @JsonProperty("error_code")
    private String errorCode;

    private String path;

    private Map<String, String> errors;

    private final Instant timestamp = Instant.now();
}
