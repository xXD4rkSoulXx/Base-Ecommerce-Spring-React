/*
 *  A pasta api é responsável por todos os Responses vindo do servidor.
 *
 *  Os Requests e os Responses dos dados de cada entidade constam na pasta dto.
 *
 *
 *  O Page Response é responsável por receber todos os dados grandes solicitados
 * pelo servidor.
 *
 *  Diferente do ApiResponse, este é destinado para listas grandes, enquanto o ApiResponse
 * é destinado para poucos dados.
 * */

package com.d4rksoul.base.ecommerce.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {
    private List<T> content;

    private int page;

    private int size;

    @JsonProperty("total_elements")
    private int totalElements;

    @JsonProperty("total_pages")
    private int totalPages;

    @JsonProperty("has_previous")
    private boolean hasPrevious;

    @JsonProperty("has_next")
    private boolean hasNext;

    @JsonProperty("is_first")
    private boolean isFirst;

    @JsonProperty("is_last")
    private boolean isLast;
}
