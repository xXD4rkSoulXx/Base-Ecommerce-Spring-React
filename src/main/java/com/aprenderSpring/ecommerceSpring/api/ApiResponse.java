/*
 *  A pasta api é responsável por todos os Responses.
 *
 *  Os Requests constam na pasta dto.
 *
 *
 *  O Api Response é responsável por receber todos os dados solicitados ao servidor.
 *
 *  Diferente do PageResponse, este é responsável por dados pequenos, enquanto
 * o PageResponse é responsável por listas grandes de dados.
 *
 * */

package com.aprenderSpring.ecommerceSpring.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;

    private String message;

    private T data;

    private final Instant timestamp = Instant.now();
}
