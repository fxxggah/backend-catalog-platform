package com.catalog.dto.product;

import com.catalog.dto.productimage.ProductImageResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;

    private BigDecimal price;
    private BigDecimal promotionalPrice;

    private Boolean visible;

    private LocalDateTime createdAt;

    private Long categoryId;

    private List<ProductImageResponse> images;
}