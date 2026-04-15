package com.catalog.dto.product;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {

    @NotBlank
    @Size(max = 250)
    private String name;

    @NotBlank
    @Size(max = 250)
    private String slug;

    private String description;

    @NotNull
    @Positive
    private BigDecimal price;

    @Positive
    private BigDecimal promotionalPrice;

    @NotNull
    private Long categoryId;

    @NotNull
    private Long storeId;

    private Boolean visible;
}