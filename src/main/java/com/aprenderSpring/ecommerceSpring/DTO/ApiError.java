package com.aprenderSpring.ecommerceSpring.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private int status;
    private String error;
    private String message;
    private String path;
    private String errorCode;
    private Map<String, String> errors;
    private final Instant timestamp = Instant.now();
}
