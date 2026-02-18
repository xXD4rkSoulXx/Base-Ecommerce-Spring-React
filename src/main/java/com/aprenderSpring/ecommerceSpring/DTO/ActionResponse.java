package com.aprenderSpring.ecommerceSpring.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionResponse {
    private boolean success;
    private String message;
    private String action;
    private Long resourceId;
    private final Instant timestamp = Instant.now();
}
