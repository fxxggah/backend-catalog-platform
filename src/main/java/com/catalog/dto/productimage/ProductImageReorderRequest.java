package com.catalog.dto.productimage;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ProductImageReorderRequest {

    @NotEmpty
    private List<Long> imageIds;
}