package com.d4rksoul.base.ecommerce.spring.react.persistence;

import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import com.d4rksoul.base.ecommerce.spring.react.entity.Order;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturn;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderReturnItem;
import com.d4rksoul.base.ecommerce.spring.react.entity.OrderShipment;
import com.d4rksoul.base.ecommerce.spring.react.entity.Product;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSku;
import com.d4rksoul.base.ecommerce.spring.react.entity.ProductSkuImage;
import com.d4rksoul.base.ecommerce.spring.react.entity.Review;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductCategory;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductColor;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnReason;
import com.d4rksoul.base.ecommerce.spring.react.enums.ReturnStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.ShipmentStatus;
import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

public final class TestData {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private TestData() {
    }

    public static Product product(String ref) {
        return Product.builder()
            .ref(ref)
            .name("Product " + ref)
            .description("Description of " + ref)
            .price(new BigDecimal("100.00"))
            .category(ProductCategory.TOPS)
            .build();
    }

    public static ProductSku sku(Product product, ProductSize size) {
        return sku(product, size, ProductColor.BLACK);
    }

    public static ProductSku sku(Product product, ProductSize size, ProductColor color) {
        return ProductSku.builder()
            .product(product)
            .size(size)
            .color(color)
            .stock(10)
            .weightGrams(500)
            .lengthCm(30)
            .widthCm(20)
            .heightCm(5)
            .build();
    }

    public static ProductSkuImage skuImage(ProductSku sku, String path) {
        return ProductSkuImage.builder()
            .productSku(sku)
            .path(path)
            .build();
    }

    public static User user() {
        return User.builder()
            .email("user" + SEQUENCE.incrementAndGet() + "@test.com")
            .password("hash")
            .firstName("Test")
            .lastName("User")
            .role(UserRole.CUSTOMER)
            .build();
    }

    public static Order order(User user, OrderStatus status) {
        return Order.builder()
            .user(user)
            .status(status)
            .currency("EUR")
            .taxAmount(new BigDecimal("0.00"))
            .shippingCost(new BigDecimal("5.00"))
            .recipientName("Test User")
            .addressLine1("Street 1")
            .city("Berlin")
            .postalCode("10117")
            .country("DE")
            .phone("+49301234567")
            .shippingCarrier("DHL")
            .shippingService("Standard")
            .build();
    }

    public static OrderItem orderItem(Order order, ProductSku sku, int quantity) {
        return OrderItem.builder()
            .order(order)
            .productSku(sku)
            .quantity(quantity)
            .price(new BigDecimal("50.00"))
            .productName("Item")
            .size(sku.getSize())
            .color(sku.getColor())
            .weightGrams(500)
            .lengthCm(30)
            .widthCm(20)
            .heightCm(5)
            .build();
    }

    public static Review review(User user, Product product, int rating, boolean hidden) {
        return Review.builder()
            .user(user)
            .product(product)
            .rating(rating)
            .title("Title")
            .description("Description")
            .hidden(hidden)
            .build();
    }

    public static DiscountCode discountCode(String code) {
        return DiscountCode.builder()
            .code(code)
            .discountPercentage(10)
            .active(true)
            .build();
    }

    public static OrderShipment shipment(Order order, ShipmentStatus status) {
        return OrderShipment.builder()
            .order(order)
            .shippoTransactionId("shippo-" + SEQUENCE.incrementAndGet())
            .status(status)
            .build();
    }

    public static OrderReturn orderReturn(Order order, ReturnStatus status) {
        return OrderReturn.builder()
            .order(order)
            .status(status)
            .reason(ReturnReason.DEFECTIVE)
            .build();
    }

    public static OrderReturnItem orderReturnItem(
        OrderReturn orderReturn, OrderItem orderItem, int quantity
    ) {
        return OrderReturnItem.builder()
            .orderReturn(orderReturn)
            .orderItem(orderItem)
            .quantity(quantity)
            .build();
    }
}
