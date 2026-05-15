package com.katallo.dto.productimage;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductImageResponse {

    private Long id;
    private String imageUrl;
    private Integer position;
}