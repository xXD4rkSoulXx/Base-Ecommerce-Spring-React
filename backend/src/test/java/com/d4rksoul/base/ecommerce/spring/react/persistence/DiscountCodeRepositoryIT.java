package com.d4rksoul.base.ecommerce.spring.react.persistence;

import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.repository.DiscountCodeRepository;
import com.d4rksoul.base.ecommerce.spring.react.specification.DiscountCodeSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.d4rksoul.base.ecommerce.spring.react.persistence.TestData.discountCode;
import static org.assertj.core.api.Assertions.assertThat;

class DiscountCodeRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private DiscountCode addCode(String code, boolean active) {
        DiscountCode discountCode = discountCode(code);

        discountCode.setActive(active);

        return persist(discountCode);
    }

    private void setCreatedAt(DiscountCode code, Instant createdAt) {
        jdbcTemplate.update(
            "update discount_codes set created_at = ? where id = ?",
            Timestamp.from(createdAt),
            code.getId()
        );
    }

    private List<Long> search(Specification<DiscountCode> specification) {
        return discountCodeRepository.findAll(specification).stream().map(DiscountCode::getId).toList();
    }

    @Test
    @DisplayName("by code: find and exists match the exact code only")
    void findAndExistsByCode() {
        DiscountCode code = addCode("SAVE10", true);
        flushAndClear();

        assertThat(discountCodeRepository.existsByCode("SAVE10")).isTrue();
        assertThat(discountCodeRepository.existsByCode("SAVE")).isFalse();
        assertThat(discountCodeRepository.findByCode("SAVE10")).get()
            .extracting(DiscountCode::getId).isEqualTo(code.getId());
        assertThat(discountCodeRepository.findByCode("SAVE")).isEmpty();
    }

    @Test
    @DisplayName("spec containsCode: part of the code, ignoring case and surrounding spaces")
    void containsCode() {
        DiscountCode summer = addCode("SUMMER20", true);
        addCode("WINTER10", true);
        flushAndClear();

        assertThat(search(DiscountCodeSpecification.containsCode(" summ ")))
            .containsExactly(summer.getId());
    }

    @Test
    @DisplayName("spec containsCode: a blank text does not filter")
    void blankSearchDoesNotFilter() {
        addCode("SUMMER20", true);
        addCode("WINTER10", true);
        flushAndClear();

        assertThat(search(DiscountCodeSpecification.containsCode("  "))).hasSize(2);
        assertThat(search(DiscountCodeSpecification.containsCode(null))).hasSize(2);
    }

    @Test
    @DisplayName("spec hasActive: filters by the flag and ignores null")
    void hasActive() {
        DiscountCode active = addCode("ACTIVE", true);
        DiscountCode inactive = addCode("INACTIVE", false);
        flushAndClear();

        assertThat(search(DiscountCodeSpecification.hasActive(true))).containsExactly(active.getId());
        assertThat(search(DiscountCodeSpecification.hasActive(false))).containsExactly(inactive.getId());
        assertThat(search(DiscountCodeSpecification.hasActive(null))).hasSize(2);
    }

    @Test
    @DisplayName("spec orderByRecentDesc: newest code first")
    void orderByRecentDesc() {
        Instant now = Instant.now();
        DiscountCode oldest = addCode("A", true);
        DiscountCode newest = addCode("B", true);
        DiscountCode middle = addCode("C", true);
        setCreatedAt(oldest, now.minus(3, ChronoUnit.DAYS));
        setCreatedAt(newest, now.minus(1, ChronoUnit.DAYS));
        setCreatedAt(middle, now.minus(2, ChronoUnit.DAYS));
        flushAndClear();

        List<Long> found = search(DiscountCodeSpecification.orderByRecentDesc());

        assertThat(found).containsExactly(newest.getId(), middle.getId(), oldest.getId());
    }
}
