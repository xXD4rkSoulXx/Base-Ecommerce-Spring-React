package com.d4rksoul.base.ecommerce.spring.react.controller;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.MergeWishlistRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{productId}")
    public ResponseEntity<Void> addToWishlist(@PathVariable Long productId) {
        wishlistService.addToWishlist(productId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/merge")
    public ResponseEntity<Void> mergeWishlist(@RequestBody MergeWishlistRequest request) {
        wishlistService.mergeWishlist(request.productIds());

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getWishlist() {
        List<ProductResponse> wishlist = wishlistService.getWishlist();

        return ResponseEntity.ok(wishlist);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Long productId) {
        wishlistService.removeFromWishlist(productId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearWishlist() {
        wishlistService.clearWishlist();

        return ResponseEntity.noContent().build();
    }
}
