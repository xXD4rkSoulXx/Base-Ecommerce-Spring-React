package com.d4rksoul.base.ecommerce.spring.react.entity;

import com.d4rksoul.base.ecommerce.spring.react.enums.TrackingEventType;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "order_tracking_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderTrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tracking_event_order"))
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrackingEventType eventType;

    private String description;

    private String location;

    @Column(nullable = false)
    private Instant occurredAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
