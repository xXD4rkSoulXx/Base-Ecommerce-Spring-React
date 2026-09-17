package com.d4rksoul.base.ecommerce.spring.react.controller;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.AddToCartRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.ApplyDiscountCodeRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.MergeCartRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.UpdateCartItemRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.CartResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody AddToCartRequest request) {
        CartResponse cart = cartService.addToCart(request.productSkuId(), request.quantity());

        return ResponseEntity.ok(cart);
    }

    @PostMapping("/merge")
    public ResponseEntity<Void> mergeCart(@RequestBody MergeCartRequest request) {
        cartService.mergeCart(request.items());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/discount")
    public ResponseEntity<CartResponse> applyDiscountCode(@RequestBody ApplyDiscountCodeRequest request) {
        CartResponse cart = cartService.applyDiscountCode(request.code());

        return ResponseEntity.ok(cart);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        CartResponse cart = cartService.getCart();

        return ResponseEntity.ok(cart);
    }

    @PutMapping("/{productSkuId}")
    public ResponseEntity<CartResponse> updateQuantity(
            @PathVariable Long productSkuId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        CartResponse cart = cartService.updateQuantity(productSkuId, request.quantity());

        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{productSkuId}")
    public ResponseEntity<CartResponse> removeFromCart(@PathVariable Long productSkuId) {
        CartResponse cart = cartService.removeFromCart(productSkuId);

        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/discount")
    public ResponseEntity<CartResponse> removeDiscountCode() {
        CartResponse cart = cartService.removeDiscountCode();

        return ResponseEntity.ok(cart);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();

        return ResponseEntity.noContent().build();
    }
}
