package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminCreateDiscountCodeRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminUpdateDiscountCodeRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDiscountCodeResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditEntityType;
import com.d4rksoul.base.ecommerce.spring.react.exception.DiscountCodeAlreadyExistsException;
import com.d4rksoul.base.ecommerce.spring.react.exception.DiscountCodeNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminDiscountCodeMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.DiscountCodeRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDiscountCodeServiceTest {

    private static final long CODE_ID = 5L;

    @Mock
    private DiscountCodeRepository discountCodeRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AdminAuditLogService adminAuditLogService;
    @Mock
    private AdminDiscountCodeMapper adminDiscountCodeMapper;
    @Mock
    private PageMapper pageMapper;

    @InjectMocks
    private AdminDiscountCodeService service;

    private final DiscountCode discountCode = DiscountCode.builder()
        .id(CODE_ID)
        .code("SAVE10")
        .discountPercentage(10)
        .active(true)
        .build();

    @Test
    @DisplayName("create: rejects a code that already exists")
    void createRejectsExistingCode() {
        AdminCreateDiscountCodeRequest request =
            new AdminCreateDiscountCodeRequest("save10", 10);

        when(discountCodeRepository.existsByCode("SAVE10")).thenReturn(true);

        assertThatThrownBy(() -> service.createDiscountCode(request))
            .isInstanceOf(DiscountCodeAlreadyExistsException.class);

        verify(discountCodeRepository, never()).save(any(DiscountCode.class));
    }

    @Test
    @DisplayName("create: saves the code in uppercase and registers it in the audit log")
    void createSavesCodeAndRegistersIt() {
        AdminCreateDiscountCodeRequest request =
            new AdminCreateDiscountCodeRequest("  save10 ", 10);
        AdminDiscountCodeResponse response = mock(AdminDiscountCodeResponse.class);

        when(discountCodeRepository.existsByCode("SAVE10")).thenReturn(false);
        when(adminDiscountCodeMapper.toEntity(request)).thenReturn(discountCode);
        when(adminDiscountCodeMapper.toDto(discountCode, 0L)).thenReturn(response);

        AdminDiscountCodeResponse result = service.createDiscountCode(request);

        assertThat(result).isSameAs(response);

        verify(discountCodeRepository).save(discountCode);
        verify(adminAuditLogService).register(
            AuditAction.CREATED,
            AuditEntityType.DISCOUNT_CODE, CODE_ID,
            "SAVE10",
            "10%"
        );
    }

    @Test
    @DisplayName("activate: turns the code on and registers it in the audit log")
    void activateTurnsCodeOn() {
        discountCode.setActive(false);

        when(discountCodeRepository.findById(CODE_ID)).thenReturn(Optional.of(discountCode));

        service.activateDiscountCode(CODE_ID);

        assertThat(discountCode.isActive()).isTrue();

        verify(adminAuditLogService).register(
            AuditAction.ACTIVATED,
            AuditEntityType.DISCOUNT_CODE,
            CODE_ID,
            "SAVE10"
        );
    }

    @Test
    @DisplayName("deactivate: turns the code off and registers it in the audit log")
    void deactivateTurnsCodeOff() {
        when(discountCodeRepository.findById(CODE_ID)).thenReturn(Optional.of(discountCode));

        service.deactivateDiscountCode(CODE_ID);

        assertThat(discountCode.isActive()).isFalse();

        verify(adminAuditLogService).register(
            AuditAction.DEACTIVATED,
            AuditEntityType.DISCOUNT_CODE,
            CODE_ID,
            "SAVE10"
        );
    }

    @Test
    @DisplayName("activate: reports a code that does not exist")
    void activateReportsUnknownCode() {
        when(discountCodeRepository.findById(CODE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activateDiscountCode(CODE_ID))
            .isInstanceOf(DiscountCodeNotFoundException.class);
    }

    @Test
    @DisplayName("update: reports a code that does not exist")
    void updateReportsUnknownCode() {
        AdminUpdateDiscountCodeRequest request = new AdminUpdateDiscountCodeRequest(20);

        when(discountCodeRepository.findById(CODE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateDiscountCode(CODE_ID, request))
            .isInstanceOf(DiscountCodeNotFoundException.class);
    }

    @Test
    @DisplayName("update: changes the percentage and the audit log shows the old and the new value")
    void updateChangesPercentageAndAuditsOldAndNewValue() {
        AdminUpdateDiscountCodeRequest request = new AdminUpdateDiscountCodeRequest(20);
        AdminDiscountCodeResponse response = mock(AdminDiscountCodeResponse.class);

        when(discountCodeRepository.findById(CODE_ID)).thenReturn(Optional.of(discountCode));
        when(orderRepository.countByDiscountCodeId(CODE_ID)).thenReturn(3L);
        when(adminDiscountCodeMapper.toDto(discountCode, 3L)).thenReturn(response);

        AdminDiscountCodeResponse result = service.updateDiscountCode(CODE_ID, request);

        assertThat(result).isSameAs(response);
        assertThat(discountCode.getDiscountPercentage()).isEqualTo(20);

        verify(adminAuditLogService).register(
            AuditAction.UPDATED,
            AuditEntityType.DISCOUNT_CODE,
            CODE_ID,
            "SAVE10",
            "10% → 20%"
        );
    }
}
