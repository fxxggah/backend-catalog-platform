package com.catalog.dto.store;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreResponse {

    private Long id;
    private String name;
    private String slug;
    private String logo;
    private String favicon;

    private String primaryColor;
    private String secondaryColor;
    private String tertiaryColor;

    private String whatsappNumber;
    private String instagram;
    private String facebook;

    private String template;
    private Boolean active;

    private String street;
    private String number;
    private String city;
    private String state;
    private String country;

    private String googleMapsLink;

    private LocalDateTime createdAt;
}