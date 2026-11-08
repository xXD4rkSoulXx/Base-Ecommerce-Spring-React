package com.d4rksoul.base.ecommerce.spring.react.dto.request.admin;

import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditEntityType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminAuditLogSearchRequest(
        String q,
        AuditEntityType entityType,
        AuditAction action,
        int page,
        int size
) {

    public AdminAuditLogSearchRequest {
        page = Math.max(page, 0);
        size = size <= 0 || size > 100 ? 20 : size;
    }
}
