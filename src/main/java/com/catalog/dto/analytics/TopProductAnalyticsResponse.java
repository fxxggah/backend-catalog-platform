package com.catalog.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopProductAnalyticsResponse {

    private Long productId;

    private String productName;

    private String productSlug;

    private long views;
}