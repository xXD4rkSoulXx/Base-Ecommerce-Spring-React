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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    private static final long WISHLIST_ID = 10L;

    @Mock
    private WishlistRepository wishlistRepository;
    @Mock
    private WishlistItemRepository wishlistItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserService userService;
    @Mock
    private ProductSkuImageService productSkuImageService;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private WishlistMapper wishlistMapper;

    @InjectMocks
    private WishlistService service;

    private final User user = User.builder()
        .id(1L)
        .build();

    private final Wishlist wishlist = Wishlist.builder()
        .id(WISHLIST_ID)
        .user(user)
        .build();

    @Test
    @DisplayName("add: creates the wishlist when the user has none and saves the product in it")
    void addCreatesWishlistWhenMissing() {
        Product product = product(5L, true);

        WishlistItem item = WishlistItem.builder()
            .product(product)
            .build();

        when(userService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(wishlistMapper.toEntity(user)).thenReturn(wishlist);
        when(wishlistRepository.save(wishlist)).thenReturn(wishlist);
        when(wishlistItemRepository.existsByWishlistIdAndProductId(WISHLIST_ID, 5L))
            .thenReturn(false);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(wishlistMapper.toEntity(wishlist, product)).thenReturn(item);

        service.addToWishlist(5L);

        verify(wishlistItemRepository).save(item);
    }

    @Test
    @DisplayName("add: a product that is already in the wishlist is not added again")
    void addIgnoresProductAlreadyInWishlist() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));
        when(wishlistItemRepository.existsByWishlistIdAndProductId(WISHLIST_ID, 5L))
            .thenReturn(true);

        service.addToWishlist(5L);

        verifyNoInteractions(productRepository);
        verify(wishlistItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("add: an inactive product is treated as not found")
    void addRejectsInactiveProduct() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));
        when(wishlistItemRepository.existsByWishlistIdAndProductId(WISHLIST_ID, 5L))
            .thenReturn(false);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product(5L, false)));

        assertThatThrownBy(() -> service.addToWishlist(5L))
            .isInstanceOf(ProductNotFoundException.class);

        verify(wishlistItemRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("merge: with no guest products it does nothing at all")
    void mergeDoesNothingWithoutGuestProducts(List<Long> guestProductIds) {
        service.mergeWishlist(guestProductIds);

        verifyNoInteractions(userService, wishlistRepository, wishlistItemRepository, productRepository);
    }

    @Test
    @DisplayName("merge: skips duplicates, products already saved and inactive products")
    void mergeAddsOnlyNewActiveProducts() {
        Product first = product(1L, true);
        Product inactive = product(3L, false);
        Product last = product(4L, true);

        WishlistItem firstItem = WishlistItem.builder()
            .product(first)
            .build();

        WishlistItem lastItem = WishlistItem.builder()
            .product(last)
            .build();

        when(userService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));
        when(wishlistItemRepository.findProductIdsByWishlistId(WISHLIST_ID))
            .thenReturn(List.of(2L));
        when(productRepository.findAllById(List.of(1L, 3L, 4L)))
            .thenReturn(List.of(first, inactive, last));
        when(wishlistMapper.toEntity(wishlist, first)).thenReturn(firstItem);
        when(wishlistMapper.toEntity(wishlist, last)).thenReturn(lastItem);

        service.mergeWishlist(List.of(1L, 1L, 2L, 3L, 4L));

        verify(wishlistItemRepository).saveAll(List.of(firstItem, lastItem));
    }

    @Test
    @DisplayName("merge: when every guest product is already saved, nothing is loaded or saved")
    void mergeDoesNothingWhenAllProductsAreAlreadySaved() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));
        when(wishlistItemRepository.findProductIdsByWishlistId(WISHLIST_ID))
            .thenReturn(List.of(2L));

        service.mergeWishlist(List.of(2L, 2L));

        verifyNoInteractions(productRepository);
        verify(wishlistItemRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("get: a user without a wishlist gets an empty list")
    void getReturnsEmptyListWithoutWishlist() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.empty());

        List<ProductResponse> result = service.getWishlist();

        assertThat(result).isEmpty();

        verifyNoInteractions(wishlistItemRepository);
    }

    @Test
    @DisplayName("get: returns only active products, each with its cover image")
    void getReturnsActiveProductsWithCover() {
        Product active = product(1L, true);
        Product inactive = product(2L, false);
        ProductResponse response = mock(ProductResponse.class);

        when(userService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));
        when(wishlistItemRepository.findByWishlistId(WISHLIST_ID)).thenReturn(List.of(
            WishlistItem.builder().product(active).build(),
            WishlistItem.builder().product(inactive).build()
        ));
        when(productSkuImageService.findCoversByProductId(List.of(1L)))
            .thenReturn(Map.of(1L, "cover.jpg"));
        when(productMapper.toDto(active, "cover.jpg")).thenReturn(response);

        List<ProductResponse> result = service.getWishlist();

        assertThat(result).containsExactly(response);
    }

    @Test
    @DisplayName("remove: deletes the product from the user's wishlist")
    void removeDeletesProductFromWishlist() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));

        service.removeFromWishlist(5L);

        verify(wishlistItemRepository).deleteByWishlistIdAndProductId(WISHLIST_ID, 5L);
    }

    @Test
    @DisplayName("remove: a user without a wishlist has nothing to remove")
    void removeDoesNothingWithoutWishlist() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.empty());

        service.removeFromWishlist(5L);

        verifyNoInteractions(wishlistItemRepository);
    }

    @Test
    @DisplayName("clear: deletes every item of the user's wishlist")
    void clearDeletesAllItems() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));

        service.clearWishlist();

        verify(wishlistItemRepository).deleteByWishlistId(WISHLIST_ID);
    }

    @Test
    @DisplayName("clear: a user without a wishlist has nothing to clear")
    void clearDoesNothingWithoutWishlist() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.empty());

        service.clearWishlist();

        verifyNoInteractions(wishlistItemRepository);
    }

    private Product product(Long id, boolean active) {
        return Product.builder().id(id).active(active).build();
    }
}
