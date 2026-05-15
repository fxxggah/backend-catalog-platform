package com.katallo.support;

import com.katallo.domain.entity.*;
import com.katallo.domain.enums.AnalyticsEventType;
import com.katallo.domain.enums.Provider;
import com.katallo.domain.enums.Role;
import com.katallo.domain.enums.StoreTemplate;
import com.katallo.dto.analytics.AnalyticsEventRequest;
import com.katallo.dto.category.CategoryRequest;
import com.katallo.dto.product.ProductRequest;
import com.katallo.dto.store.StoreRequest;
import com.katallo.dto.storeinvite.StoreInviteRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static User user() {
        return user(1L, "Gabriel", "gabriel@email.com");
    }

    public static User user(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setProvider(Provider.GOOGLE);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    public static Store store() {
        return store(1L, "minha-loja", "Minha Loja");
    }

    public static Store store(Long id, String slug, String name) {
        Store store = new Store();
        store.setId(id);
        store.setName(name);
        store.setSlug(slug);
        store.setTemplate(StoreTemplate.MINIMAL);
        store.setActive(true);
        store.setWhatsappNumber("5514999999999");
        store.setInstagram("minhaloja");
        store.setFacebook("minhaloja");
        store.setCreatedAt(LocalDateTime.now());
        store.setUpdatedAt(LocalDateTime.now());
        return store;
    }

    public static Category category(Store store) {
        return category(1L, "vestidos", "Vestidos", store);
    }

    public static Category category(Long id, String slug, String name, Store store) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setSlug(slug);
        category.setStore(store);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }

    public static Product product(Store store, Category category) {
        return product(1L, "vestido-floral", "Vestido Floral", store, category);
    }

    public static Product product(Long id, String slug, String name, Store store, Category category) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setSlug(slug);
        product.setDescription("Descrição do produto");
        product.setPrice(BigDecimal.valueOf(100.00));
        product.setPromotionalPrice(BigDecimal.valueOf(89.90));
        product.setCategory(category);
        product.setStore(store);
        product.setFeatured(false);
        product.setInStock(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    public static ProductImage productImage(Product product) {
        ProductImage image = new ProductImage();
        image.setId(1L);
        image.setProduct(product);
        image.setImageUrl("https://res.cloudinary.com/demo/image.jpg");
        image.setPosition(1);
        return image;
    }

    public static StoreUser storeUser(Store store, User user, Role role) {
        StoreUser storeUser = new StoreUser();
        storeUser.setId(1L);
        storeUser.setStore(store);
        storeUser.setUser(user);
        storeUser.setRole(role);
        storeUser.setCreatedAt(LocalDateTime.now());
        return storeUser;
    }

    public static StoreInvite storeInvite(Store store) {
        StoreInvite invite = new StoreInvite();
        invite.setId(1L);
        invite.setStore(store);
        invite.setEmail("admin@email.com");
        invite.setToken("token-convite");
        invite.setCreatedBy(1L);
        invite.setExpiresAt(LocalDateTime.now().plusDays(2));
        invite.setCreatedAt(LocalDateTime.now());
        return invite;
    }

    public static AnalyticsEvent analyticsEvent(Store store, Product product, AnalyticsEventType type) {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setId(1L);
        event.setStore(store);
        event.setProduct(product);
        event.setEventType(type);
        event.setSessionId("session-123");
        event.setReferrer("instagram");
        event.setUserAgent("Mozilla");
        event.setCreatedAt(LocalDateTime.now());
        return event;
    }

    public static StoreRequest storeRequest() {
        return StoreRequest.builder()
                .name("Minha Loja")
                .template(StoreTemplate.MINIMAL)
                .whatsappNumber("5514999999999")
                .instagram("@minhaloja")
                .facebook("minhaloja")
                .build();
    }

    public static CategoryRequest categoryRequest() {
        return CategoryRequest.builder()
                .name("Vestidos")
                .build();
    }

    public static ProductRequest productRequest(Long categoryId) {
        return ProductRequest.builder()
                .name("Vestido Floral")
                .description("Descrição do produto")
                .price(BigDecimal.valueOf(100.00))
                .promotionalPrice(BigDecimal.valueOf(89.90))
                .categoryId(categoryId)
                .featured(false)
                .inStock(true)
                .build();
    }

    public static StoreInviteRequest storeInviteRequest() {
        return StoreInviteRequest.builder()
                .email("admin@email.com")
                .build();
    }

    public static AnalyticsEventRequest analyticsEventRequest() {
        return AnalyticsEventRequest.builder()
                .sessionId("session-123")
                .referrer("instagram")
                .userAgent("Mozilla")
                .build();
    }
}
