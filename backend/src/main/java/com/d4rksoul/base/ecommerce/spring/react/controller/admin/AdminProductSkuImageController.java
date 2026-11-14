package com.d4rksoul.base.ecommerce.spring.react.controller.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminProductSkuImageResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminProductSkuImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products/{productId}/skus/{skuId}/images")
@RequiredArgsConstructor
public class AdminProductSkuImageController {

    private final AdminProductSkuImageService adminProductSkuImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCTS_WRITE')")
    public ResponseEntity<AdminProductSkuImageResponse> uploadImage(
        @PathVariable Long productId, @PathVariable Long skuId, @RequestParam("file") MultipartFile file
    ) {
        AdminProductSkuImageResponse image = adminProductSkuImageService.uploadImage(productId, skuId, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(image);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCTS_READ')")
    public ResponseEntity<List<AdminProductSkuImageResponse>> getImages(
        @PathVariable Long productId, @PathVariable Long skuId
    ) {
        List<AdminProductSkuImageResponse> images = adminProductSkuImageService.getImages(productId, skuId);

        return ResponseEntity.ok(images);
    }

    @PutMapping("/{imageId}/product-cover")
    @PreAuthorize("hasAuthority('PRODUCTS_WRITE')")
    public ResponseEntity<Void> setProductCover(
        @PathVariable Long productId, @PathVariable Long skuId, @PathVariable Long imageId
    ) {
        adminProductSkuImageService.setProductCover(productId, skuId, imageId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{imageId}/sku-cover")
    @PreAuthorize("hasAuthority('PRODUCTS_WRITE')")
    public ResponseEntity<Void> setSkuCover(
        @PathVariable Long productId, @PathVariable Long skuId, @PathVariable Long imageId
    ) {
        adminProductSkuImageService.setSkuCover(productId, skuId, imageId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasAuthority('PRODUCTS_WRITE')")
    public ResponseEntity<Void> deleteImage(
        @PathVariable Long productId, @PathVariable Long skuId, @PathVariable Long imageId
    ) {
        adminProductSkuImageService.deleteImage(productId, skuId, imageId);

        return ResponseEntity.noContent().build();
    }
}
