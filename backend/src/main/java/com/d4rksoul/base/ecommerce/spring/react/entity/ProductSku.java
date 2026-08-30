package com.d4rksoul.base.ecommerce.spring.react.entity;

import com.d4rksoul.base.ecommerce.spring.react.enums.ProductColor;
import com.d4rksoul.base.ecommerce.spring.react.enums.ProductSize;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "product_skus",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_size_color", columnNames = {"product_id", "size", "color"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_sku_product"))
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductSize size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductColor color;

    @Min(0)
    @Column(nullable = false)
    private int stock;

    @Min(0)
    @Max(100)
    private int discountPercentage;

    @Builder.Default
    @OneToMany(mappedBy = "productSku", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSkuImage> productSkuImages = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "productSku", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "productSku")
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "productSku", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WishlistItem> wishlistItems = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
