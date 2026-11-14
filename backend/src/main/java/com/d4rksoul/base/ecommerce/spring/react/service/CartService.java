package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.constant.Constants;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.MergeCartItemRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.CartItemResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductSkuRepository productSkuRepository;
    private final DiscountCodeRepository discountCodeRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductSkuImageService productSkuImageService;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    @Transactional
    public CartResponse addToCart(Long productSkuId, int quantity) {
        User currentUser = userService.getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
            .orElseGet(() -> cartRepository.save(cartMapper.toEntity(currentUser)));

        ProductSku sku = productSkuRepository.findByIdAndActiveTrueAndProductActiveTrue(productSkuId)
            .orElseThrow(() -> new ProductSkuNotFoundException(
                "Product SKU not found with id: " + productSkuId
            ));

        CartItem existingItem = cartItemRepository.findByCartIdAndProductSkuId(cart.getId(), productSkuId)
            .orElse(null);
        if (existingItem != null) {
            existingItem.setQuantity(
                Math.min(existingItem.getQuantity() + quantity, Constants.MAX_CART_ITEM_QUANTITY)
            );
        } else {
            long cartItemCount = cartItemRepository.countByCartId(cart.getId());
            if (cartItemCount >= Constants.MAX_CART_LINES) {
                throw new CartLimitException(
                    "Cart cannot have more than " + Constants.MAX_CART_LINES + " different items."
                );
            }

            CartItem cartItem = cartItemMapper.toEntity(cart, sku, quantity);

            cartItemRepository.save(cartItem);
        }

        return buildCartResponse(cart);
    }

    @Transactional
    public void mergeCart(List<MergeCartItemRequest> guestItems) {
        if (guestItems == null || guestItems.isEmpty()) {
            return;
        }

        User currentUser = userService.getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
            .orElseGet(() -> cartRepository.save(cartMapper.toEntity(currentUser)));

        long lines = cartItemRepository.countByCartId(cart.getId());

        for (MergeCartItemRequest guestItem : guestItems) {
            CartItem existingItem = cartItemRepository
                .findByCartIdAndProductSkuId(cart.getId(), guestItem.productSkuId())
                .orElse(null);
            if (existingItem != null) {
                existingItem.setQuantity(Math.min(
                    existingItem.getQuantity() + guestItem.quantity(), Constants.MAX_CART_ITEM_QUANTITY
                    ));
            } else if (lines < Constants.MAX_CART_LINES) {
                Optional<ProductSku> sku = productSkuRepository
                    .findByIdAndActiveTrueAndProductActiveTrue(guestItem.productSkuId());
                if (sku.isPresent()) {
                    CartItem cartItem = cartItemMapper.toEntity(cart, sku.get(), guestItem.quantity());
                    cartItemRepository.save(cartItem);
                    lines++;
                }
            }
        }
    }

    @Transactional
    public CartResponse applyDiscountCode(String code) {
        User currentUser = userService.getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
            .orElseGet(() -> cartRepository.save(cartMapper.toEntity(currentUser)));

        DiscountCode discountCode = discountCodeRepository.findByCode(code.trim().toUpperCase(Locale.ROOT))
            .filter(DiscountCode::isActive)
            .orElseThrow(() -> new InvalidDiscountCodeException("Invalid or inactive discount code."));

        boolean discountCodeAlreadyUsed = orderRepository.existsByUserIdAndDiscountCodeIdAndStatusNotIn(
            currentUser.getId(), discountCode.getId(), OrderStatus.DISCOUNT_UNUSED_STATUSES
        );
        if (discountCodeAlreadyUsed) {
            throw new InvalidDiscountCodeException("You have already used this discount code.");
        }

        cart.setDiscountCode(discountCode);

        return buildCartResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart() {
        User currentUser = userService.getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElse(null);

        if (cart == null) {
            return cartMapper.toDto(List.of(), null);
        }

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse updateQuantity(Long productSkuId, int quantity) {
        User currentUser = userService.getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElse(null);

        if (cart == null) {
            return cartMapper.toDto(List.of(), null);
        }

        cartItemRepository.findByCartIdAndProductSkuId(cart.getId(), productSkuId)
            .ifPresent(item -> item.setQuantity(quantity));

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse removeFromCart(Long productSkuId) {
        User currentUser = userService.getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElse(null);

        if (cart == null) {
            return cartMapper.toDto(List.of(), null);
        }

        cartItemRepository.deleteByCartIdAndProductSkuId(cart.getId(), productSkuId);

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse removeDiscountCode() {
        User currentUser = userService.getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElse(null);

        if (cart == null) {
            return cartMapper.toDto(List.of(), null);
        }

        cart.setDiscountCode(null);

        return buildCartResponse(cart);
    }

    @Transactional
    public void clearCart() {
        User currentUser = userService.getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElse(null);

        if (cart == null) {
            return;
        }

        cartItemRepository.deleteByCartId(cart.getId());
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartIdOrderByIdAsc(cart.getId());

        Map<Long, String> covers = productSkuImageService.findCoversBySkuId(
            items.stream().map(CartItem::getProductSku).toList()
        );

        List<CartItemResponse> itemResponses = items.stream()
            .map(item ->
                cartItemMapper.toDto(item, covers.get(item.getProductSku().getId()))
            )
            .toList();

        return cartMapper.toDto(itemResponses, cart.getDiscountCode());
    }
}
