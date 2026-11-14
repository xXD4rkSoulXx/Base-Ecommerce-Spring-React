package com.d4rksoul.base.ecommerce.spring.react.controller.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminCreateDiscountCodeRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminDiscountCodeSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminUpdateDiscountCodeRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDiscountCodeResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminDiscountCodeService;
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
@RequestMapping("/api/admin/discount-codes")
@RequiredArgsConstructor
public class AdminDiscountCodeController {

    private final AdminDiscountCodeService adminDiscountCodeService;

    @PostMapping
    @PreAuthorize("hasAuthority('DISCOUNTS_WRITE')")
    public ResponseEntity<AdminDiscountCodeResponse> createDiscountCode(
        @Valid @RequestBody AdminCreateDiscountCodeRequest request
    ) {
        AdminDiscountCodeResponse discountCode = adminDiscountCodeService.createDiscountCode(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(discountCode);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('DISCOUNTS_WRITE')")
    public ResponseEntity<Void> activateDiscountCode(@PathVariable Long id) {
        adminDiscountCodeService.activateDiscountCode(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('DISCOUNTS_WRITE')")
    public ResponseEntity<Void> deactivateDiscountCode(@PathVariable Long id) {
        adminDiscountCodeService.deactivateDiscountCode(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DISCOUNTS_READ')")
    public ResponseEntity<PageResponse<AdminDiscountCodeResponse>> getDiscountCodes(
        @ModelAttribute AdminDiscountCodeSearchRequest filter
    ) {
        PageResponse<AdminDiscountCodeResponse> discountCodes = adminDiscountCodeService
            .getDiscountCodes(filter);

        return ResponseEntity.ok(discountCodes);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DISCOUNTS_WRITE')")
    public ResponseEntity<AdminDiscountCodeResponse> updateDiscountCode(
        @PathVariable Long id, @Valid @RequestBody AdminUpdateDiscountCodeRequest request
    ) {
        AdminDiscountCodeResponse discountCode = adminDiscountCodeService.updateDiscountCode(id, request);

        return ResponseEntity.ok(discountCode);
    }
}
