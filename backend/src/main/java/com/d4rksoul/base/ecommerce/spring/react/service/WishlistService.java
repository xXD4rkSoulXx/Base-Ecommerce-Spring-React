package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.projection.ProductCoverProjection;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.entity.Wishlist;
import com.d4rksoul.base.ecommerce.spring.react.entity.WishlistItem;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.exception.UserNotAuthenticatedException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.ProductMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuImageRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.WishlistItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.WishlistRepository;
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
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final ProductSkuImageRepository productSkuImageRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    @Transactional
    public void addToWishlist(Long productId) {
        User currentUser = getCurrentUser();
        Wishlist wishlist = wishlistRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> wishlistRepository.save(Wishlist.builder().user(currentUser).build()));

        if (wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), productId)) {
            return;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        WishlistItem item = WishlistItem.builder()
                .wishlist(wishlist)
                .product(product)
                .build();

        wishlistItemRepository.save(item);
    }

    @Transactional
    public void mergeWishlist(List<Long> guestProductIds) {
        if (guestProductIds == null || guestProductIds.isEmpty()) {
            return;
        }

        User currentUser = getCurrentUser();
        Wishlist wishlist = wishlistRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> wishlistRepository.save(Wishlist.builder().user(currentUser).build()));

        List<Long> existingProductIds = wishlistItemRepository.findProductIdsByWishlistId(wishlist.getId());

        List<Long> productIdsToAdd = guestProductIds.stream()
                .distinct()
                .filter(id -> !existingProductIds.contains(id))
                .toList();

        if (productIdsToAdd.isEmpty()) {
            return;
        }

        List<Product> productsToAdd = productRepository.findAllById(productIdsToAdd);

        List<WishlistItem> newItems = productsToAdd.stream()
                .map(product -> WishlistItem.builder()
                        .wishlist(wishlist)
                        .product(product)
                        .build())
                .toList();

        wishlistItemRepository.saveAll(newItems);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getWishlist() {
        User currentUser = getCurrentUser();
        Wishlist wishlist = wishlistRepository.findByUserId(currentUser.getId()).orElse(null);

        if (wishlist == null) {
            return List.of();
        }

        List<WishlistItem> items = wishlistItemRepository.findByWishlistId(wishlist.getId());

        List<Product> products = items.stream()
                .map(WishlistItem::getProduct)
                .toList();

        List<Long> productIds = products.stream()
                .map(Product::getId)
                .toList();

        Map<Long, String> covers = productSkuImageRepository.findCoverByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        ProductCoverProjection::productId,
                        ProductCoverProjection::path,
                        (existing, _) -> existing
                ));

        return products.stream()
                .map(product -> productMapper.toDto(product, covers.get(product.getId())))
                .toList();
    }

    @Transactional
    public void removeFromWishlist(Long productId) {
        User currentUser = getCurrentUser();
        Wishlist wishlist = wishlistRepository.findByUserId(currentUser.getId()).orElse(null);

        if (wishlist == null) {
            return;
        }

        wishlistItemRepository.deleteByWishlistIdAndProductId(wishlist.getId(), productId);
    }

    @Transactional
    public void clearWishlist() {
        User currentUser = getCurrentUser();
        Wishlist wishlist = wishlistRepository.findByUserId(currentUser.getId()).orElse(null);

        if (wishlist == null) {
            return;
        }

        wishlistItemRepository.deleteByWishlistId(wishlist.getId());
    }

    private User getCurrentUser() {
        Jwt jwt = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> (Jwt) auth.getPrincipal())
                .orElseThrow(() -> new UserNotAuthenticatedException("User is not authenticated"));

        return userRepository.findByEmail(jwt.getSubject())
                .orElseThrow(() -> new UserNotAuthenticatedException("User is not authenticated"));
    }
}
