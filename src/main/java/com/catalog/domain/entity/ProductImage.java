package com.catalog.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "product_images",
        indexes = {
                @Index(name = "idx_product_image_product", columnList = "product_id")
        }
)
@Getter
@Setter
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Integer position;
}