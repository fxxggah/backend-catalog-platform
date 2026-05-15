package com.katallo.dto.product;

import com.katallo.dto.productimage.ProductImageResponse;
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

    private Boolean inStock;
    private Boolean featured;

    private LocalDateTime createdAt;

    private Long categoryId;

    private List<ProductImageResponse> images;
}