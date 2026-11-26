package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.exception.InsufficientStockException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidStockAdjustmentException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductSkuNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.PageMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminStockMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.ProductSkuImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStockServiceTest {

    private static final long SKU_ID = 5L;

    @Mock
    private ProductSkuRepository productSkuRepository;
    @Mock
    private ProductSkuImageService productSkuImageService;
    @Mock
    private AdminAuditLogService adminAuditLogService;
    @Mock
    private AdminStockMapper adminStockMapper;
    @Mock
    private PageMapper pageMapper;

    @InjectMocks
    private AdminStockService service;

    @Test
    @DisplayName("adjust stock: rejects an adjustment of zero without touching the database")
    void adjustStockRejectsZero() {
        assertThatThrownBy(() -> service.adjustStock(SKU_ID, 0))
            .isInstanceOf(InvalidStockAdjustmentException.class);

        verifyNoInteractions(productSkuRepository, adminAuditLogService);
    }

    @ParameterizedTest(name = "adjustment {0} leaves stock {1}")
    @CsvSource({
        "3, 8, '+3 (stock now 8)'",
        "-2, 3, '-2 (stock now 3)'"
    })
    @DisplayName("adjust stock: registers the signed adjustment and the new stock in the audit log")
    void adjustStockRegistersAdjustment(int delta, int stockNow, String expectedDetails) {
        ProductSku sku = ProductSku.builder()
            .id(SKU_ID)
            .stock(stockNow)
            .build();

        when(productSkuRepository.adjustStock(SKU_ID, delta)).thenReturn(1);
        when(productSkuRepository.findById(SKU_ID)).thenReturn(Optional.of(sku));

        service.adjustStock(SKU_ID, delta);

        verify(adminAuditLogService).registerVariant(AuditAction.STOCK_ADJUSTED, sku, expectedDetails);
    }

    @Test
    @DisplayName("adjust stock: refuses to take the stock below zero")
    void adjustStockRejectsStockBelowZero() {
        when(productSkuRepository.adjustStock(SKU_ID, -10)).thenReturn(0);
        when(productSkuRepository.existsById(SKU_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.adjustStock(SKU_ID, -10))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessageContaining("below zero");

        verifyNoInteractions(adminAuditLogService);
    }

    @Test
    @DisplayName("adjust stock: reports a SKU that does not exist")
    void adjustStockReportsUnknownSku() {
        when(productSkuRepository.adjustStock(SKU_ID, 3)).thenReturn(0);
        when(productSkuRepository.existsById(SKU_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.adjustStock(SKU_ID, 3))
            .isInstanceOf(ProductSkuNotFoundException.class);

        verifyNoInteractions(adminAuditLogService);
    }

    @Test
    @DisplayName("set stock: sets the value and registers it in the audit log")
    void setStockRegistersNewValue() {
        ProductSku sku = ProductSku.builder()
            .id(SKU_ID)
            .stock(12)
            .build();

        when(productSkuRepository.setStock(SKU_ID, 12)).thenReturn(1);
        when(productSkuRepository.findById(SKU_ID)).thenReturn(Optional.of(sku));

        service.setStock(SKU_ID, 12);

        verify(adminAuditLogService).registerVariant(AuditAction.STOCK_SET, sku, "Set to 12");
    }

    @Test
    @DisplayName("set stock: reports a SKU that does not exist")
    void setStockReportsUnknownSku() {
        when(productSkuRepository.setStock(SKU_ID, 12)).thenReturn(0);

        assertThatThrownBy(() -> service.setStock(SKU_ID, 12))
            .isInstanceOf(ProductSkuNotFoundException.class);

        verify(adminAuditLogService, never()).registerVariant(any(), any(), any());
    }
}
