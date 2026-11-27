package com.d4rksoul.base.ecommerce.spring.react.persistence;

import com.d4rksoul.base.ecommerce.spring.react.entity.AuditLog;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditEntityType;
import com.d4rksoul.base.ecommerce.spring.react.repository.AuditLogRepository;
import com.d4rksoul.base.ecommerce.spring.react.specification.AuditLogSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    private AuditLog addLog(
        String actorEmail, String entityLabel, AuditEntityType entityType, AuditAction action
    ) {
        return persist(AuditLog.builder()
            .actorEmail(actorEmail)
            .action(action)
            .entityType(entityType)
            .entityId(1L)
            .entityLabel(entityLabel)
            .build());
    }

    private AuditLog addLog(String actorEmail, String entityLabel) {
        return addLog(actorEmail, entityLabel, AuditEntityType.PRODUCT, AuditAction.UPDATED);
    }

    private AuditLog addLog(AuditEntityType entityType, AuditAction action) {
        return addLog("admin@mail.com", "Label", entityType, action);
    }

    private void setCreatedAt(AuditLog log, Instant createdAt) {
        jdbcTemplate.update(
            "update audit_logs set created_at = ? where id = ?",
            Timestamp.from(createdAt),
            log.getId()
        );
    }

    private List<Long> search(Specification<AuditLog> specification) {
        return auditLogRepository.findAll(specification).stream().map(AuditLog::getId).toList();
    }

    @Test
    @DisplayName("spec search: matches the email of the actor ignoring case")
    void searchMatchesActorEmail() {
        AuditLog ana = addLog("ana@mail.com", "Rain Jacket");
        addLog("rui@mail.com", "Basic Tee");
        flushAndClear();

        assertThat(search(AuditLogSpecification.containsQuery(" ANA@ "))).containsExactly(ana.getId());
    }

    @Test
    @DisplayName("spec search: matches the label of the entity")
    void searchMatchesEntityLabel() {
        addLog("ana@mail.com", "Rain Jacket");
        AuditLog tee = addLog("ana@mail.com", "Basic Tee");
        flushAndClear();

        assertThat(search(AuditLogSpecification.containsQuery("basic"))).containsExactly(tee.getId());
    }

    @Test
    @DisplayName("spec search: a blank text does not filter")
    void blankSearchDoesNotFilter() {
        addLog("ana@mail.com", "Rain Jacket");
        addLog("rui@mail.com", "Basic Tee");
        flushAndClear();

        assertThat(search(AuditLogSpecification.containsQuery("  "))).hasSize(2);
        assertThat(search(AuditLogSpecification.containsQuery(null))).hasSize(2);
    }

    @Test
    @DisplayName("spec hasEntityType: filters by entity type and ignores null")
    void hasEntityType() {
        AuditLog product = addLog(AuditEntityType.PRODUCT, AuditAction.UPDATED);
        addLog(AuditEntityType.ORDER, AuditAction.UPDATED);
        flushAndClear();

        assertThat(search(AuditLogSpecification.hasEntityType(AuditEntityType.PRODUCT)))
            .containsExactly(product.getId());
        assertThat(search(AuditLogSpecification.hasEntityType(null))).hasSize(2);
    }

    @Test
    @DisplayName("spec hasAction: filters by action and ignores null")
    void hasAction() {
        AuditLog created = addLog(AuditEntityType.PRODUCT, AuditAction.CREATED);
        addLog(AuditEntityType.PRODUCT, AuditAction.ARCHIVED);
        flushAndClear();

        assertThat(search(AuditLogSpecification.hasAction(AuditAction.CREATED)))
            .containsExactly(created.getId());
        assertThat(search(AuditLogSpecification.hasAction(null))).hasSize(2);
    }

    @Test
    @DisplayName("spec filters combine: every condition must hold")
    void filtersCombine() {
        AuditLog match = addLog(AuditEntityType.ORDER, AuditAction.STATUS_CHANGED);
        addLog(AuditEntityType.ORDER, AuditAction.REFUNDED);
        addLog(AuditEntityType.PRODUCT, AuditAction.STATUS_CHANGED);
        flushAndClear();

        Specification<AuditLog> specification = Specification
            .where(AuditLogSpecification.hasEntityType(AuditEntityType.ORDER))
            .and(AuditLogSpecification.hasAction(AuditAction.STATUS_CHANGED));

        assertThat(search(specification)).containsExactly(match.getId());
    }


    @Test
    @DisplayName("spec orderByRecentDesc: newest entry first")
    void orderByRecentDesc() {
        Instant now = Instant.now();
        AuditLog oldest = addLog(AuditEntityType.PRODUCT, AuditAction.CREATED);
        AuditLog newest = addLog(AuditEntityType.PRODUCT, AuditAction.UPDATED);
        AuditLog middle = addLog(AuditEntityType.PRODUCT, AuditAction.ARCHIVED);
        setCreatedAt(oldest, now.minus(3, ChronoUnit.DAYS));
        setCreatedAt(newest, now.minus(1, ChronoUnit.DAYS));
        setCreatedAt(middle, now.minus(2, ChronoUnit.DAYS));
        flushAndClear();

        List<Long> found = search(AuditLogSpecification.orderByRecentDesc());

        assertThat(found).containsExactly(newest.getId(), middle.getId(), oldest.getId());
    }
}
