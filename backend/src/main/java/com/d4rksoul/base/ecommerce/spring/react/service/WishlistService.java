package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProductResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.entity.Wishlist;
import com.d4rksoul.base.ecommerce.spring.react.entity.WishlistItem;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.ProductMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.WishlistMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.WishlistItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final ProductSkuImageService productSkuImageService;
    private final ProductMapper productMapper;
    private final WishlistMapper wishlistMapper;

    @Transactional
    public void addToWishlist(Long productId) {
        User currentUser = userService.getCurrentUser();
        Wishlist wishlist = wishlistRepository.findByUserId(currentUser.getId())
            .orElseGet(() -> {
                Wishlist newWishlist = wishlistMapper.toEntity(currentUser);

                return wishlistRepository.save(newWishlist);
            });

        boolean alreadyInWishlist = wishlistItemRepository
            .existsByWishlistIdAndProductId(wishlist.getId(), productId);
        if (alreadyInWishlist) {
            return;
        }

        Product product = productRepository.findById(productId)
            .filter(Product::isActive)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        WishlistItem item = wishlistMapper.toEntity(wishlist, product);

        wishlistItemRepository.save(item);
    }

    @Transactional
    public void mergeWishlist(List<Long> guestProductIds) {
        if (guestProductIds == null || guestProductIds.isEmpty()) {
            return;
        }

        User currentUser = userService.getCurrentUser();
        Wishlist wishlist = wishlistRepository.findByUserId(currentUser.getId())
            .orElseGet(() -> {
                Wishlist newWishlist = wishlistMapper.toEntity(currentUser);

                return wishlistRepository.save(newWishlist);
            });

        List<Long> existingProductIds = wishlistItemRepository.findProductIdsByWishlistId(wishlist.getId());

        List<Long> productIdsToAdd = guestProductIds.stream()
            .distinct()
            .filter(id -> !existingProductIds.contains(id))
            .toList();

        if (productIdsToAdd.isEmpty()) {
            return;
        }

        List<Product> productsToAdd = productRepository.findAllById(productIdsToAdd).stream()
            .filter(Product::isActive)
            .toList();

        List<WishlistItem> newItems = productsToAdd.stream()
            .map(product -> wishlistMapper.toEntity(wishlist, product))
            .toList();

        wishlistItemRepository.saveAll(newItems);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getWishlist() {
        User currentUser = userService.getCurrentUser();
        Wishlist wishlist = wishlistRepository.findByUserId(currentUser.getId()).orElse(null);

        if (wishlist == null) {
            return List.of();
        }

        List<WishlistItem> items = wishlistItemRepository.findByWishlistId(wishlist.getId());

        List<Product> products = items.stream()
            .map(WishlistItem::getProduct)
            .filter(Product::isActive)
            .toList();

        List<Long> productIds = products.stream()
            .map(Product::getId)
            .toList();

        Map<Long, String> covers = productSkuImageService.findCoversByProductId(productIds);

        return products.stream()
            .map(product ->
                productMapper.toDto(product, covers.get(product.getId()))
            )
            .toList();
    }

    @Transactional
    public void removeFromWishlist(Long productId) {
        User currentUser = userService.getCurrentUser();
        Wishlist wishlist = wishlistRepository.findByUserId(currentUser.getId()).orElse(null);

        if (wishlist == null) {
            return;
        }

        wishlistItemRepository.deleteByWishlistIdAndProductId(wishlist.getId(), productId);
    }

    @Transactional
    public void clearWishlist() {
        User currentUser = userService.getCurrentUser();
        Wishlist wishlist = wishlistRepository.findByUserId(currentUser.getId()).orElse(null);

        if (wishlist == null) {
            return;
        }

        wishlistItemRepository.deleteByWishlistId(wishlist.getId());
    }
}
