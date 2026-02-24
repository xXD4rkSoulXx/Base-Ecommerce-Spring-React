/*
 *  A pasta api é responsável por todos os Responses.
 *
 *  Os Requests constam na pasta dto.
 *
 *
 *  O Page Response é responsável por receber todos os dados grandes solicitados
 * pelo servidor.
 *
 *  Diferente do ApiResponse, este é destinado para listas grandes, enquanto o ApiResponse
 * é destinado para poucos dados.
 * */

package com.aprenderSpring.ecommerceSpring.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private int totalElements;
    private int totalPages;
    private boolean hasPrevious;
    private boolean hasNext;
    private boolean isFirst;
    private boolean isLast;
}
