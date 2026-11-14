package com.d4rksoul.base.ecommerce.spring.react.dto.request;

import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnReason;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateReturnRequest(
    @NotNull(message = "Return reason is required.")
    ReturnReason reason,

    @Size(max = 500, message = "Comment cannot exceed 500 characters.")
    String comment,

    @NotEmpty(message = "Select at least one item to return.")
    @Size(max = 50, message = "Too many items.")
    List<@NotNull(message = "Items cannot be null.") @Valid CreateReturnItemRequest> items
) {}
