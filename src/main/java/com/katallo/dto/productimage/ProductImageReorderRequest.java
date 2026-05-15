package com.katallo.dto.productimage;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ProductImageReorderRequest {

    @NotEmpty
    private List<Long> imageIds;
}