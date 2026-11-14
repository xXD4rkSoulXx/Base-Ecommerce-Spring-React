package com.d4rksoul.base.ecommerce.spring.react.controller.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminProductRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminProductSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminProductDetailsResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminProductSummaryResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCTS_WRITE')")
    public ResponseEntity<AdminProductDetailsResponse> createProduct(
        @Valid @RequestBody AdminProductRequest request
    ) {
        AdminProductDetailsResponse product = adminProductService.createProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('PRODUCTS_WRITE')")
    public ResponseEntity<Void> archiveProduct(@PathVariable Long id) {
        adminProductService.archiveProduct(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('PRODUCTS_WRITE')")
    public ResponseEntity<Void> restoreProduct(@PathVariable Long id) {
        adminProductService.restoreProduct(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCTS_READ')")
    public ResponseEntity<PageResponse<AdminProductSummaryResponse>> getProducts(
        @ModelAttribute AdminProductSearchRequest filter
    ) {
        PageResponse<AdminProductSummaryResponse> products = adminProductService.getProducts(filter);

        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCTS_READ')")
    public ResponseEntity<AdminProductDetailsResponse> getProduct(@PathVariable Long id) {
        AdminProductDetailsResponse product = adminProductService.getProduct(id);

        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCTS_WRITE')")
    public ResponseEntity<AdminProductDetailsResponse> updateProduct(
        @PathVariable Long id, @Valid @RequestBody AdminProductRequest request
    ) {
        AdminProductDetailsResponse product = adminProductService.updateProduct(id, request);

        return ResponseEntity.ok(product);
    }
}
