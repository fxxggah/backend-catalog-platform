package com.katallo.controller.admin;

import com.katallo.annotation.CurrentUser;
import com.katallo.dto.analytics.AnalyticsSummaryResponse;
import com.katallo.dto.analytics.DailyVisitsResponse;
import com.katallo.dto.analytics.TopProductAnalyticsResponse;
import com.katallo.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/stores/{storeSlug}/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryResponse> getSummary(
            @PathVariable String storeSlug,
            @CurrentUser Long userId) {

        return ResponseEntity.ok(
                analyticsService.getSummary(storeSlug, userId)
        );
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductAnalyticsResponse>> getTopProducts(
            @PathVariable String storeSlug,
            @RequestParam(defaultValue = "5") int limit,
            @CurrentUser Long userId) {

        return ResponseEntity.ok(
                analyticsService.getTopProducts(storeSlug, userId, limit)
        );
    }

    @GetMapping("/daily-visits")
    public ResponseEntity<List<DailyVisitsResponse>> getDailyVisits(
            @PathVariable String storeSlug,
            @RequestParam(defaultValue = "7") int days,
            @CurrentUser Long userId) {

        return ResponseEntity.ok(
                analyticsService.getDailyVisits(storeSlug, userId, days)
        );
    }
}