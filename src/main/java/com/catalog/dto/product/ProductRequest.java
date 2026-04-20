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

    private String description;

    @NotNull
    @Positive
    private BigDecimal price;

    @Positive
    private BigDecimal promotionalPrice;

    @NotNull
    private Long categoryId;

    private Boolean visible;
}