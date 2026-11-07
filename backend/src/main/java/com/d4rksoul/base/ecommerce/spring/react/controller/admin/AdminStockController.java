package com.d4rksoul.base.ecommerce.spring.react.controller.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminAdjustStockRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminStockSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminSetStockRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminStockItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stock")
@RequiredArgsConstructor
public class AdminStockController {

    private final AdminStockService adminStockService;

    @PostMapping("/{skuId}/adjustments")
    @PreAuthorize("hasAuthority('STOCK_WRITE')")
    public ResponseEntity<Void> adjustStock(
            @PathVariable Long skuId,
            @Valid @RequestBody AdminAdjustStockRequest request
    ) {
        adminStockService.adjustStock(skuId, request.delta());

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STOCK_READ')")
    public ResponseEntity<PageResponse<AdminStockItemResponse>> getStock(
            @ModelAttribute AdminStockSearchRequest filter
    ) {
        PageResponse<AdminStockItemResponse> stock = adminStockService.getStock(filter);

        return ResponseEntity.ok(stock);
    }

    @PutMapping("/{skuId}")
    @PreAuthorize("hasAuthority('STOCK_WRITE')")
    public ResponseEntity<Void> setStock(
            @PathVariable Long skuId,
            @Valid @RequestBody AdminSetStockRequest request
    ) {
        adminStockService.setStock(skuId, request.stock());

        return ResponseEntity.noContent().build();
    }
}
