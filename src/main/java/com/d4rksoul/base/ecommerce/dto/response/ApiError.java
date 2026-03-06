/*
 *  A pasta dto contém todos os requests e responses.
 *
 *
 *  O Api Error é responsável por receber os detalhes dos erros que ocorrem.
 * */

package com.d4rksoul.base.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
