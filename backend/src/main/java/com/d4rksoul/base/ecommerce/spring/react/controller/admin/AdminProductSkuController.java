package com.d4rksoul.base.ecommerce.spring.react.controller.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminCreateProductSkuRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminUpdateProductSkuRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminProductSkuResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminProductSkuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products/{productId}/skus")
@RequiredArgsConstructor
public class AdminProductSkuController {

    private final AdminProductSkuService adminProductSkuService;

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCTS_WRITE')")
    public ResponseEntity<AdminProductSkuResponse> createSku(
        @PathVariable Long productId, @Valid @RequestBody AdminCreateProductSkuRequest request
    ) {
        AdminProductSkuResponse sku = adminProductSkuService.createSku(productId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(sku);
    }

    @PostMapping("/{skuId}/archive")
    @PreAuthorize("hasAuthority('PRODUCTS_WRITE')")
    public ResponseEntity<Void> archiveSku(@PathVariable Long productId, @PathVariable Long skuId) {
        adminProductSkuService.archiveSku(productId, skuId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{skuId}/restore")
    @PreAuthorize("hasAuthority('PRODUCTS_WRITE')")
    public ResponseEntity<Void> restoreSku(@PathVariable Long productId, @PathVariable Long skuId) {
        adminProductSkuService.restoreSku(productId, skuId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCTS_READ')")
    public ResponseEntity<List<AdminProductSkuResponse>> getSkus(@PathVariable Long productId) {
        List<AdminProductSkuResponse> skus = adminProductSkuService.getSkus(productId);

        return ResponseEntity.ok(skus);
    }

    @PutMapping("/{skuId}")
    @PreAuthorize("hasAuthority('PRODUCTS_WRITE')")
    public ResponseEntity<AdminProductSkuResponse> updateSku(
        @PathVariable Long productId,
        @PathVariable Long skuId,
        @Valid @RequestBody AdminUpdateProductSkuRequest request
    ) {
        AdminProductSkuResponse sku = adminProductSkuService.updateSku(productId, skuId, request);

        return ResponseEntity.ok(sku);
    }
}
