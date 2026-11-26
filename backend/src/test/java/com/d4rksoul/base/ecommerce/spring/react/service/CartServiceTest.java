package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.MergeCartItemRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.CartResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.Cart;
import com.d4rksoul.base.ecommerce.spring.react.entity.CartItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.CartLimitException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidDiscountCodeException;
import com.d4rksoul.base.ecommerce.spring.react.exception.ProductSkuNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.CartItemMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.CartMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.DiscountCodeRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.ProductSkuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final long CART_ID = 1L;
    private static final long SKU_ID = 10L;

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductSkuRepository productSkuRepository;
    @Mock
    private DiscountCodeRepository discountCodeRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserService userService;
    @Mock
    private ProductSkuImageService productSkuImageService;

    private CartService service;

    private final User user = User.builder()
        .id(1L)
        .build();

    private final Cart cart = Cart.builder()
        .id(CART_ID)
        .user(user)
        .build();

    private final ProductSku sku = ProductSku.builder()
        .id(SKU_ID)
        .build();

    @BeforeEach
    void setUp() {
        service = new CartService(
            cartRepository,
            cartItemRepository,
            productSkuRepository,
            discountCodeRepository,
            orderRepository,
            userService,
            productSkuImageService,
            new CartMapper(),
            new CartItemMapper()
        );
    }

    @Test
    @DisplayName("caps the quantity of an existing line at the maximum")
    void addToCartCapsQuantityOfExistingLine() {
        CartItem existing = CartItem.builder()
            .cart(cart)
            .productSku(sku)
            .quantity(8)
            .build();

        givenCartAndSku();

        when(cartItemRepository.findByCartIdAndProductSkuId(CART_ID, SKU_ID))
            .thenReturn(Optional.of(existing));

        service.addToCart(SKU_ID, 5);

        assertThat(existing.getQuantity()).isEqualTo(Constants.MAX_CART_ITEM_QUANTITY);
    }

    @Test
    @DisplayName("adds to the quantity of an existing line below the maximum")
    void addToCartAddsQuantityToExistingLine() {
        CartItem existing = CartItem.builder()
            .cart(cart)
            .productSku(sku)
            .quantity(2)
            .build();

        givenCartAndSku();

        when(cartItemRepository.findByCartIdAndProductSkuId(CART_ID, SKU_ID))
            .thenReturn(Optional.of(existing));

        service.addToCart(SKU_ID, 3);

        assertThat(existing.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("creates a new line when the cart is below the line limit")
    void addToCartCreatesNewLine() {
        givenCartAndSku();

        when(cartItemRepository.findByCartIdAndProductSkuId(CART_ID, SKU_ID))
            .thenReturn(Optional.empty());
        when(cartItemRepository.countByCartId(CART_ID))
            .thenReturn((long) Constants.MAX_CART_LINES - 1);

        service.addToCart(SKU_ID, 2);

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("rejects a new line when the cart already has the maximum number of lines")
    void addToCartRejectsNewLineAtLimit() {
        givenCartAndSku();

        when(cartItemRepository.findByCartIdAndProductSkuId(CART_ID, SKU_ID))
            .thenReturn(Optional.empty());
        when(cartItemRepository.countByCartId(CART_ID))
            .thenReturn((long) Constants.MAX_CART_LINES);

        assertThatThrownBy(() -> service.addToCart(SKU_ID, 1))
            .isInstanceOf(CartLimitException.class);

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("rejects a SKU that is inactive or does not exist")
    void addToCartRejectsUnavailableSku() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(productSkuRepository.findByIdAndActiveTrueAndProductActiveTrue(SKU_ID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addToCart(SKU_ID, 1))
            .isInstanceOf(ProductSkuNotFoundException.class);

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("applies a valid code after trimming and uppercasing it")
    void applyDiscountCodeNormalizesCode() {
        DiscountCode code = DiscountCode.builder()
            .id(5L)
            .code("SAVE10")
            .discountPercentage(10)
            .active(true)
            .build();

        givenCart();

        when(discountCodeRepository.findByCode("SAVE10")).thenReturn(Optional.of(code));
        when(orderRepository.existsByUserIdAndDiscountCodeIdAndStatusNotIn(
            user.getId(), code.getId(), OrderStatus.DISCOUNT_UNUSED_STATUSES
        )).thenReturn(false);

        CartResponse response = service.applyDiscountCode("  save10 ");

        assertThat(cart.getDiscountCode()).isSameAs(code);
        assertThat(response.discountCode()).isEqualTo("SAVE10");
        assertThat(response.discountPercentage()).isEqualTo(10);
    }

    @Test
    @DisplayName("rejects a code that does not exist")
    void applyDiscountCodeRejectsUnknownCode() {
        givenCart();

        when(discountCodeRepository.findByCode("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.applyDiscountCode("nope"))
            .isInstanceOf(InvalidDiscountCodeException.class)
            .hasMessageContaining("Invalid or inactive");
        assertThat(cart.getDiscountCode()).isNull();
    }

    @Test
    @DisplayName("rejects a code that is inactive")
    void applyDiscountCodeRejectsInactiveCode() {
        DiscountCode code = DiscountCode.builder()
            .id(5L)
            .code("OLD")
            .discountPercentage(10)
            .active(false)
            .build();

        givenCart();

        when(discountCodeRepository.findByCode("OLD")).thenReturn(Optional.of(code));

        assertThatThrownBy(() -> service.applyDiscountCode("old"))
            .isInstanceOf(InvalidDiscountCodeException.class)
            .hasMessageContaining("Invalid or inactive");
        assertThat(cart.getDiscountCode()).isNull();
    }

    @Test
    @DisplayName("rejects a code the user already used in a real order")
    void applyDiscountCodeRejectsAlreadyUsedCode() {
        DiscountCode code = DiscountCode.builder()
            .id(5L)
            .code("SAVE10")
            .discountPercentage(10)
            .active(true)
            .build();

        givenCart();

        when(discountCodeRepository.findByCode("SAVE10")).thenReturn(Optional.of(code));
        when(orderRepository.existsByUserIdAndDiscountCodeIdAndStatusNotIn(
            user.getId(), code.getId(), OrderStatus.DISCOUNT_UNUSED_STATUSES
        )).thenReturn(true);

        assertThatThrownBy(() -> service.applyDiscountCode("SAVE10"))
            .isInstanceOf(InvalidDiscountCodeException.class)
            .hasMessageContaining("already used");
        assertThat(cart.getDiscountCode()).isNull();
    }

    @Test
    @DisplayName("does nothing when merging an empty guest cart")
    void mergeCartIgnoresEmptyGuestCart() {
        service.mergeCart(List.of());
        service.mergeCart(null);

        verifyNoInteractions(userService, cartRepository, cartItemRepository);
    }

    @Test
    @DisplayName("merges guest quantities into an existing line and caps them")
    void mergeCartCapsQuantityOfExistingLine() {
        CartItem existing = CartItem.builder()
            .cart(cart)
            .productSku(sku)
            .quantity(7)
            .build();

        givenCart();

        when(cartItemRepository.countByCartId(CART_ID)).thenReturn(1L);
        when(cartItemRepository.findByCartIdAndProductSkuId(CART_ID, SKU_ID))
            .thenReturn(Optional.of(existing));

        service.mergeCart(List.of(new MergeCartItemRequest(SKU_ID, 6)));

        assertThat(existing.getQuantity()).isEqualTo(Constants.MAX_CART_ITEM_QUANTITY);
    }

    @Test
    @DisplayName("skips guest items whose SKU is no longer available")
    void mergeCartSkipsUnavailableSku() {
        givenCart();

        when(cartItemRepository.countByCartId(CART_ID)).thenReturn(0L);
        when(cartItemRepository.findByCartIdAndProductSkuId(CART_ID, SKU_ID))
            .thenReturn(Optional.empty());
        when(productSkuRepository.findByIdAndActiveTrueAndProductActiveTrue(SKU_ID))
            .thenReturn(Optional.empty());

        service.mergeCart(List.of(new MergeCartItemRequest(SKU_ID, 1)));

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("skips new guest lines once the cart reached the line limit")
    void mergeCartSkipsNewLinesAtLimit() {
        givenCart();

        when(cartItemRepository.countByCartId(CART_ID))
            .thenReturn((long) Constants.MAX_CART_LINES);
        when(cartItemRepository.findByCartIdAndProductSkuId(CART_ID, SKU_ID))
            .thenReturn(Optional.empty());

        service.mergeCart(List.of(new MergeCartItemRequest(SKU_ID, 1)));

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    private void givenCart() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
    }

    private void givenCartAndSku() {
        givenCart();

        when(productSkuRepository.findByIdAndActiveTrueAndProductActiveTrue(SKU_ID))
            .thenReturn(Optional.of(sku));
    }
}
