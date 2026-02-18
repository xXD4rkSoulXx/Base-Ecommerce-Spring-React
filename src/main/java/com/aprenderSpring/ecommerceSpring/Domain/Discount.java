package com.aprenderSpring.ecommerceSpring.Domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "discounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    @Min(0)
    @Max(100)
    private int percentage;

    @Column(name = "start_date", nullable = false)
    @NotBlank
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @NotBlank
    private LocalDate endDate;

    @OneToOne(mappedBy = "discount")
    private Product product;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;
}
