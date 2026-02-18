package com.aprenderSpring.ecommerceSpring.DTO;

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
