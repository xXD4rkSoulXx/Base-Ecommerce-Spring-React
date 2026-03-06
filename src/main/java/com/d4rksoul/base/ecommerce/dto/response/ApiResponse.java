/*
 *  A pasta dto contém todos os requests e responses.
 *
 *
 *  O Api Response é responsável por receber todos os dados solicitados ao servidor.
 *
 *  Diferente do PageResponse, este é responsável por dados pequenos, enquanto
 * o PageResponse é responsável por listas grandes de dados.
 *
 * */

package com.d4rksoul.base.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class ApiResponse<T> {
    private boolean success;

    private String message;

    private T data;

    private final Instant timestamp = Instant.now();
}
