package com.catalog.dto.store;

import com.catalog.domain.enums.StoreTemplate;
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

    private String logo;
    private String favicon;

    private String whatsappNumber;
    private String instagram;
    private String facebook;

    private StoreTemplate template;

    private String street;
    private String number;
    private String city;
    private String state;
    private String country;

    private String googleMapsLink;

}
