package com.d4rksoul.base.ecommerce.spring.react.dto.response.admin;

import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditEntityType;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminAuditLogResponse(
    Long id,

    String actorEmail,

    AuditAction action,

    AuditEntityType entityType,

    Long entityId,

    String entityLabel,

    @Nullable String details,

    Instant createdAt
) {}
