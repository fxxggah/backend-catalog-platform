package com.catalog.dto.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 100)
    private String slug;

    private String logo;

    private String primaryColor;
    private String secondaryColor;
    private String tertiaryColor;

    private String whatsappNumber;
    private String instagram;
    private String facebook;

    private String template;

    private String street;
    private String number;
    private String city;
    private String state;
    private String country;

    private String googleMapsLink;

}
