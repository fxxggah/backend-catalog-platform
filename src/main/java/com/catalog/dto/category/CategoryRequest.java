package com.catalog.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {

    @NotBlank
    @Size(max = 250)
    private String name;

    @NotBlank
    @Size(max = 250)
    private String slug;

    @NotNull
    private Long storeId;
}