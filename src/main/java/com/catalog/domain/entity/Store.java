package com.catalog.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "stores",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_store_slug", columnNames = {"slug"})
        }
)
@Getter
@Setter
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(length = 500)
    private String logo;

    @Column(length = 20)
    private String primaryColor;

    @Column(length = 20)
    private String secondaryColor;

    @Column(length = 20)
    private String tertiaryColor;

    @Column(length = 20)
    private String whatsappNumber;

    @Column(length = 100)
    private String instagram;

    @Column(length = 100)
    private String facebook;

    @Column(length = 50)
    private String template;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(length = 100)
    private String street;

    @Column(length = 10)
    private String number;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String state;

    @Column(length = 50)
    private String country;

    @Column(length = 500)
    private String googleMapsLink;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long createdBy;
    private Long updatedBy;
}