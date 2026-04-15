package com.catalog.dto.productimage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductImageRequest {

    @NotNull
    private Long productId;

    @NotBlank
    private String imageUrl;

    private Integer position;
}