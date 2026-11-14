package com.d4rksoul.base.ecommerce.spring.react.mapper.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminAuditLogResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.AuditLog;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditEntityType;
import org.springframework.stereotype.Component;

@Component
public class AdminAuditLogMapper {

    public AuditLog toEntity(
        String actorEmail,
        AuditAction action,
        AuditEntityType entityType,
        Long entityId,
        String entityLabel,
        String details
    ) {
        return AuditLog.builder()
            .actorEmail(actorEmail)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .entityLabel(entityLabel)
            .details(details)
            .build();
    }

    public AdminAuditLogResponse toDto(AuditLog auditLog) {
        return AdminAuditLogResponse.builder()
            .id(auditLog.getId())
            .actorEmail(auditLog.getActorEmail())
            .action(auditLog.getAction())
            .entityType(auditLog.getEntityType())
            .entityId(auditLog.getEntityId())
            .entityLabel(auditLog.getEntityLabel())
            .details(auditLog.getDetails())
            .createdAt(auditLog.getCreatedAt())
            .build();
    }
}
