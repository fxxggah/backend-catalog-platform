package com.catalog.domain.entity;

import com.catalog.domain.enums.AnalyticsEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "analytics_events",
        indexes = {
                @Index(name = "idx_analytics_store", columnList = "store_id"),
                @Index(name = "idx_analytics_product", columnList = "product_id"),
                @Index(name = "idx_analytics_type", columnList = "event_type"),
                @Index(name = "idx_analytics_created_at", columnList = "created_at"),
                @Index(name = "idx_analytics_store_type_created", columnList = "store_id, event_type, created_at")
        }
)
@Getter
@Setter
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Loja onde o evento aconteceu.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    /**
     * Produto relacionado ao evento.
     * Pode ser null em eventos como STORE_VIEW e WHATSAPP_CLICK geral.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private AnalyticsEventType eventType;

    @Column(length = 100)
    private String sessionId;

    @Column(length = 500)
    private String referrer;

    @Column(length = 500)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}