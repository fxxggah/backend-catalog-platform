package com.catalog.service;

import com.catalog.domain.entity.AnalyticsEvent;
import com.catalog.domain.entity.Product;
import com.catalog.domain.entity.Store;
import com.catalog.domain.enums.AnalyticsEventType;
import com.catalog.dto.analytics.AnalyticsEventRequest;
import com.catalog.dto.analytics.AnalyticsSummaryResponse;
import com.catalog.dto.analytics.DailyVisitsResponse;
import com.catalog.dto.analytics.TopProductAnalyticsResponse;
import com.catalog.repository.AnalyticsEventRepository;
import com.catalog.repository.ProductRepository;
import com.catalog.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final int DEFAULT_TOP_PRODUCTS_LIMIT = 5;
    private static final int DEFAULT_DAILY_VISITS_DAYS = 7;

    private final AnalyticsEventRepository analyticsEventRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final AccessControlService accessControlService;

    public void registerStoreView(String storeSlug, AnalyticsEventRequest request) {
        Store store = getStoreBySlug(storeSlug);

        saveEvent(store, null, AnalyticsEventType.STORE_VIEW, request);
    }

    public void registerProductView(
            String storeSlug,
            String productSlug,
            AnalyticsEventRequest request
    ) {
        Store store = getStoreBySlug(storeSlug);

        Product product = productRepository
                .findByStoreIdAndSlugAndDeletedAtIsNull(store.getId(), productSlug)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (!product.getVisible()) {
            throw new RuntimeException("Produto não disponível");
        }

        saveEvent(store, product, AnalyticsEventType.PRODUCT_VIEW, request);
    }

    public void registerWhatsappClick(String storeSlug, AnalyticsEventRequest request) {
        Store store = getStoreBySlug(storeSlug);

        saveEvent(store, null, AnalyticsEventType.WHATSAPP_CLICK, request);
    }

    public AnalyticsSummaryResponse getSummary(String storeSlug, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        accessControlService.checkAdminAccess(userId, store.getId());

        long storeViews = analyticsEventRepository.countByStoreIdAndEventType(
                store.getId(),
                AnalyticsEventType.STORE_VIEW
        );

        long productViews = analyticsEventRepository.countByStoreIdAndEventType(
                store.getId(),
                AnalyticsEventType.PRODUCT_VIEW
        );

        long whatsappClicks = analyticsEventRepository.countByStoreIdAndEventType(
                store.getId(),
                AnalyticsEventType.WHATSAPP_CLICK
        );

        List<TopProductAnalyticsResponse> topProducts =
                getTopProducts(storeSlug, userId, 1);

        TopProductAnalyticsResponse mostViewedProduct =
                topProducts.isEmpty() ? null : topProducts.get(0);

        return AnalyticsSummaryResponse.builder()
                .storeViews(storeViews)
                .productViews(productViews)
                .whatsappClicks(whatsappClicks)
                .mostViewedProductId(
                        mostViewedProduct != null ? mostViewedProduct.getProductId() : null
                )
                .mostViewedProductName(
                        mostViewedProduct != null ? mostViewedProduct.getProductName() : null
                )
                .mostViewedProductViews(
                        mostViewedProduct != null ? mostViewedProduct.getViews() : 0
                )
                .build();
    }

    public List<TopProductAnalyticsResponse> getTopProducts(
            String storeSlug,
            Long userId,
            int limit
    ) {
        Store store = getStoreBySlug(storeSlug);

        accessControlService.checkAdminAccess(userId, store.getId());

        int safeLimit = normalizeLimit(limit);

        return analyticsEventRepository
                .findTopProductsByEventType(
                        store.getId(),
                        AnalyticsEventType.PRODUCT_VIEW,
                        PageRequest.of(0, safeLimit)
                )
                .stream()
                .map(row -> TopProductAnalyticsResponse.builder()
                        .productId((Long) row[0])
                        .productName((String) row[1])
                        .productSlug((String) row[2])
                        .views((Long) row[3])
                        .build())
                .toList();
    }

    public List<DailyVisitsResponse> getDailyVisits(
            String storeSlug,
            Long userId,
            int days
    ) {
        Store store = getStoreBySlug(storeSlug);

        accessControlService.checkAdminAccess(userId, store.getId());

        int safeDays = normalizeDays(days);
        LocalDateTime start = LocalDate.now()
                .minusDays(safeDays - 1L)
                .atStartOfDay();

        return analyticsEventRepository
                .findDailyVisits(
                        store.getId(),
                        AnalyticsEventType.STORE_VIEW,
                        start
                )
                .stream()
                .map(row -> DailyVisitsResponse.builder()
                        .date(convertToLocalDate(row[0]))
                        .visits((Long) row[1])
                        .build())
                .toList();
    }

    private void saveEvent(
            Store store,
            Product product,
            AnalyticsEventType eventType,
            AnalyticsEventRequest request
    ) {
        AnalyticsEvent event = new AnalyticsEvent();

        event.setStore(store);
        event.setProduct(product);
        event.setEventType(eventType);
        event.setCreatedAt(LocalDateTime.now());

        if (request != null) {
            event.setSessionId(trim(request.getSessionId(), 100));
            event.setReferrer(trim(request.getReferrer(), 500));
            event.setUserAgent(trim(request.getUserAgent(), 500));
        }

        analyticsEventRepository.save(event);
    }

    private Store getStoreBySlug(String storeSlug) {
        return storeRepository.findBySlug(storeSlug)
                .orElseThrow(() -> new RuntimeException("Loja não encontrada"));
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_TOP_PRODUCTS_LIMIT;
        }

        return Math.min(limit, 20);
    }

    private int normalizeDays(int days) {
        if (days <= 0) {
            return DEFAULT_DAILY_VISITS_DAYS;
        }

        return Math.min(days, 30);
    }

    private String trim(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.length() <= maxLength
                ? trimmed
                : trimmed.substring(0, maxLength);
    }

    private LocalDate convertToLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }

        if (value instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        return LocalDate.parse(value.toString());
    }
}