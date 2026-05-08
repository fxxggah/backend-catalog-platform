package com.catalog.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsSummaryResponse {

    private long storeViews;

    private long productViews;

    private long whatsappClicks;

    private String mostViewedProductName;

    private Long mostViewedProductId;

    private long mostViewedProductViews;
}