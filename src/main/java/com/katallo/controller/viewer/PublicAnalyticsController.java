package com.katallo.controller.viewer;

import com.katallo.dto.analytics.AnalyticsEventRequest;
import com.katallo.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stores/{storeSlug}/analytics")
@RequiredArgsConstructor
public class PublicAnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/store-view")
    public ResponseEntity<Void> registerStoreView(
            @PathVariable String storeSlug,
            @RequestBody(required = false) AnalyticsEventRequest request) {

        analyticsService.registerStoreView(storeSlug, request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/whatsapp-click")
    public ResponseEntity<Void> registerWhatsappClick(
            @PathVariable String storeSlug,
            @RequestBody(required = false) AnalyticsEventRequest request) {

        analyticsService.registerWhatsappClick(storeSlug, request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/products/{productSlug}/view")
    public ResponseEntity<Void> registerProductView(
            @PathVariable String storeSlug,
            @PathVariable String productSlug,
            @RequestBody(required = false) AnalyticsEventRequest request) {

        analyticsService.registerProductView(storeSlug, productSlug, request);

        return ResponseEntity.noContent().build();
    }
}