package com.d4rksoul.base.ecommerce.spring.react.service;

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
import com.d4rksoul.base.ecommerce.spring.react.exception.UserNotAuthenticatedException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.CartItemMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.CartMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartItemRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.CartRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.DiscountCodeRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
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

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductImageService productImageService;
    private final DiscountCodeRepository discountCodeRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

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
            existingItem.setQuantity(Math.min(existingItem.getQuantity() + quantity, 10));
            cartItemRepository.save(existingItem);
        } else {
            if (cartItemRepository.countByCartId(cart.getId()) >= 20) {
                throw new CartLimitException("Cart cannot have more than 20 different items.");
            }

            CartItem item = cartItemMapper.toEntity(cart, sku, quantity);
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

        long lines = cartItemRepository.countByCartId(cart.getId());

        for (MergeCartItemRequest guestItem : guestItems) {
            CartItem existingItem = cartItemRepository
                    .findByCartIdAndProductSkuId(cart.getId(), guestItem.productSkuId())
                    .orElse(null);

            if (existingItem != null) {
                existingItem.setQuantity(Math.min(existingItem.getQuantity() + guestItem.quantity(), 10));
                cartItemRepository.save(existingItem);
            } else if (lines < 20) {
                Optional<ProductSku> sku = productSkuRepository.findById(guestItem.productSkuId());

                if (sku.isPresent()) {
                    cartItemRepository.save(cartItemMapper.toEntity(cart, sku.get(), guestItem.quantity()));
                    lines++;
                }
            }
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

        boolean discountCodeAlreadyUsed = orderRepository.existsByUserIdAndDiscountCodeIdAndStatusNotIn(
                currentUser.getId(),
                discountCode.getId(),
                List.of(OrderStatus.PENDING, OrderStatus.FAILED, OrderStatus.CANCELED, OrderStatus.REFUNDED)
        );

        if (discountCodeAlreadyUsed) {
            throw new InvalidDiscountCodeException("You have already used this discount code.");
        }

        cart.setDiscountCode(discountCode);
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart() {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElse(null);

        if (cart == null) {
            return cartMapper.toDto(List.of(), null);
        }

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse updateQuantity(Long productSkuId, int quantity) {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElse(null);

        if (cart == null) {
            return cartMapper.toDto(List.of(), null);
        }

        cartItemRepository.findByCartIdAndProductSkuId(cart.getId(), productSkuId)
                .ifPresent(item -> {
                    item.setQuantity(quantity);
                    cartItemRepository.save(item);
                });

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse removeFromCart(Long productSkuId) {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElse(null);

        if (cart == null) {
            return cartMapper.toDto(List.of(), null);
        }

        cartItemRepository.deleteByCartIdAndProductSkuId(cart.getId(), productSkuId);

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse removeDiscountCode() {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElse(null);

        if (cart == null) {
            return cartMapper.toDto(List.of(), null);
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
        List<CartItem> items = cartItemRepository.findByCartIdOrderByIdAsc(cart.getId());

        Map<Long, String> covers = productImageService.findCoversBySkuId(
                items.stream().map(CartItem::getProductSku).toList()
        );

        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> cartItemMapper.toDto(item, covers.get(item.getProductSku().getId())))
                .toList();

        return cartMapper.toDto(itemResponses, cart.getDiscountCode());
    }

    private User getCurrentUser() {
        Jwt jwt = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> (Jwt) auth.getPrincipal())
                .orElseThrow(() -> new UserNotAuthenticatedException("User is not authenticated"));

        return userRepository.findByEmail(jwt.getSubject())
                .orElseThrow(() -> new UserNotAuthenticatedException("User is not authenticated"));
    }
}
