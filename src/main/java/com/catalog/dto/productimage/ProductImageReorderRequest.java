package com.catalog.dto.productimage;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProductImageReorderRequest {

    @NotNull
    private Long productId;

    @NotEmpty
    private List<Long> imageIds;
}