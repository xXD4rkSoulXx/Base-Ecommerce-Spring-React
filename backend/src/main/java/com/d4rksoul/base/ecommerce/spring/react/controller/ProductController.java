package com.d4rksoul.base.ecommerce.spring.react.controller;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.ProductSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.ProductShopRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(@ModelAttribute ProductShopRequest filter) {
        PageResponse<ProductResponse> productsPage = productService.getProducts(filter);

        return ResponseEntity.ok(productsPage);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProductResponse>> search(@ModelAttribute ProductSearchRequest search) {
        PageResponse<ProductResponse> searchPage = productService.search(search);

        return ResponseEntity.ok(searchPage);
    }
}
