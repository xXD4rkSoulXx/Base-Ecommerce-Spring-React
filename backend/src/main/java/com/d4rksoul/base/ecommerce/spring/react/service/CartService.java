package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductCoverProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductSkuCoverProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.MergeCartItemRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.CartItemResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.CartResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Cart;
import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidDiscountCodeException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductSkuNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.UserNotAuthenticatedException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.CartMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.DiscountCodeRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuImageRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductSkuImageRepository productSkuImageRepository;
    private final DiscountCodeRepository discountCodeRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Transactional
    public CartResponse addToCart(Long productSkuId, int quantity) {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> cartRepository.save(cartMapper.toEntity(currentUser)));

        ProductSku sku = productSkuRepository.findById(productSkuId)
                .orElseThrow(() -> new ProductSkuNotFoundException(
                        "Product SKU not found with id: " + productSkuId
                ));

        CartItem existingItem = cartItemRepository.findByCartIdAndProductSkuId(cart.getId(), productSkuId)
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem item = cartMapper.toEntity(cart, sku, quantity);
            cartItemRepository.save(item);
        }

        return buildCartResponse(cart);
    }

    @Transactional
    public void mergeCart(List<MergeCartItemRequest> guestItems) {
        if (guestItems == null || guestItems.isEmpty()) {
            return;
        }

        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> cartRepository.save(cartMapper.toEntity(currentUser)));

        for (MergeCartItemRequest guestItem : guestItems) {
            CartItem existingItem = cartItemRepository
                    .findByCartIdAndProductSkuId(cart.getId(), guestItem.productSkuId())
                    .orElse(null);

            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + guestItem.quantity());
                cartItemRepository.save(existingItem);
                continue;
            }

            productSkuRepository.findById(guestItem.productSkuId()).ifPresent(sku -> {
                CartItem item = cartMapper.toEntity(cart, sku, guestItem.quantity());

                cartItemRepository.save(item);
            });
        }
    }

    @Transactional
    public CartResponse applyDiscountCode(String code) {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> cartRepository.save(cartMapper.toEntity(currentUser)));

        DiscountCode discountCode = discountCodeRepository.findByCode(code)
                .filter(DiscountCode::isActive)
                .orElseThrow(() -> new InvalidDiscountCodeException("Invalid or inactive discount code."));

        cart.setDiscountCode(discountCode);
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart() {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElse(null);

        if (cart == null) {
            return cartMapper.toCartDto(List.of(), null);
        }

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse updateQuantity(Long productSkuId, int quantity) {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElse(null);

        if (cart == null) {
            return cartMapper.toCartDto(List.of(), null);
        }

        if (quantity <= 0) {
            cartItemRepository.deleteByCartIdAndProductSkuId(cart.getId(), productSkuId);
        } else {
            cartItemRepository.findByCartIdAndProductSkuId(cart.getId(), productSkuId)
                    .ifPresent(item -> {
                        item.setQuantity(quantity);
                        cartItemRepository.save(item);
                    });
        }

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse removeFromCart(Long productSkuId) {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElse(null);

        if (cart == null) {
            return cartMapper.toCartDto(List.of(), null);
        }

        cartItemRepository.deleteByCartIdAndProductSkuId(cart.getId(), productSkuId);

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse removeDiscountCode() {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElse(null);

        if (cart == null) {
            return cartMapper.toCartDto(List.of(), null);
        }

        cart.setDiscountCode(null);
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    @Transactional
    public void clearCart() {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElse(null);

        if (cart == null) {
            return;
        }

        cartItemRepository.deleteByCartId(cart.getId());
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        List<Long> productSkuIds = items.stream()
                .map(item -> item.getProductSku().getId())
                .distinct()
                .toList();

        Map<Long, String> skuCovers = productSkuImageRepository.findImagesByProductSkuIds(productSkuIds)
                .stream()
                .collect(Collectors.toMap(
                        ProductSkuCoverProjection::productSkuId,
                        ProductSkuCoverProjection::path,
                        (existing, _) -> existing
                ));

        List<Long> productIds = items.stream()
                .map(item -> item.getProductSku().getProduct().getId())
                .distinct()
                .toList();

        Map<Long, String> productCovers = productSkuImageRepository.findCoverByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        ProductCoverProjection::productId,
                        ProductCoverProjection::path,
                        (existing, _) -> existing
                ));

        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> {
                    Long skuId = item.getProductSku().getId();
                    Long productId = item.getProductSku().getProduct().getId();
                    String cover = skuCovers.getOrDefault(skuId, productCovers.get(productId));

                    return cartMapper.toDto(item, cover);
                })
                .toList();

        DiscountCode discountCode = cart.getDiscountCode();

        return CartResponse.builder()
                .items(itemResponses)
                .discountCode(discountCode != null ? discountCode.getCode() : null)
                .discountPercentage(discountCode != null ? discountCode.getDiscountPercentage() : 0)
                .build();
    }

    private User getCurrentUser() {
        Jwt jwt = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> (Jwt) auth.getPrincipal())
                .orElseThrow(() -> new UserNotAuthenticatedException("User is not authenticated"));

        return userRepository.findByEmail(jwt.getSubject())
                .orElseThrow(() -> new UserNotAuthenticatedException("User is not authenticated"));
    }
}
