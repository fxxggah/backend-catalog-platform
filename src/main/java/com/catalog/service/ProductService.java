package com.catalog.service;

import com.catalog.domain.entity.Product;
import com.catalog.dto.product.ProductRequest;
import com.catalog.dto.product.ProductResponse;
import com.catalog.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.catalog.domain.entity.Category;
import com.catalog.domain.entity.Store;
import com.catalog.repository.CategoryRepository;
import com.catalog.repository.StoreRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final AccessControlService access;

    public ProductResponse create(String storeSlug, ProductRequest req, Long userId) {

        Store store = getStoreBySlug(storeSlug);

        access.checkAdminAccess(userId, store.getId());

        if (req.getPromotionalPrice() != null &&
                req.getPromotionalPrice().compareTo(req.getPrice()) >= 0) {
            throw new RuntimeException("Preço promocional inválido");
        }

        productRepository.findByStoreIdAndSlug(store.getId(), req.getSlug())
                .ifPresent(p -> { throw new RuntimeException("Slug já existe"); });

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        Product p = new Product();
        p.setName(req.getName());
        p.setSlug(req.getSlug());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setPromotionalPrice(req.getPromotionalPrice());
        p.setCategory(category);
        p.setStore(store);
        p.setVisible(req.getVisible() != null ? req.getVisible() : true);
        p.setCreatedAt(LocalDateTime.now());
        p.setCreatedBy(userId);

        return map(productRepository.save(p));
    }

    public Page<ProductResponse> list(String storeSlug, String search, Pageable pageable) {

        Store store = getStoreBySlug(storeSlug);

        Page<Product> page;

        if (search != null && !search.isBlank()) {
            page = productRepository
                    .findByStoreIdAndNameContainingIgnoreCaseAndDeletedAtIsNull(
                            store.getId(), search, pageable);
        } else {
            page = productRepository
                    .findByStoreIdAndVisibleTrueAndDeletedAtIsNull(
                            store.getId(), pageable);
        }

        return page.map(this::map);
    }

    public Page<ProductResponse> listByCategory(String storeSlug, String categorySlug, Pageable pageable) {

        Store store = getStoreBySlug(storeSlug);

        Category category = categoryRepository
                .findByStoreIdAndSlug(store.getId(), categorySlug)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        Page<Product> page = productRepository
                .findByStoreIdAndCategoryIdAndVisibleTrueAndDeletedAtIsNull(
                        store.getId(),
                        category.getId(),
                        pageable
                );

        return page.map(this::map);
    }

    public void delete(String storeSlug, Long id, Long userId) {

        Store store = getStoreBySlug(storeSlug);

        access.checkAdminAccess(userId, store.getId());

        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (!p.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Produto não pertence à loja");
        }

        p.setDeletedAt(LocalDateTime.now());
        productRepository.save(p);
    }

    public ProductResponse getBySlug(String storeSlug, String productSlug) {

        Store store = getStoreBySlug(storeSlug);

        Product p = productRepository
                .findByStoreIdAndSlug(store.getId(), productSlug)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (p.getDeletedAt() != null || !p.getVisible()) {
            throw new RuntimeException("Produto não disponível");
        }

        return map(p);
    }

    public ProductResponse update(String storeSlug, Long id, ProductRequest req, Long userId) {

        Store store = getStoreBySlug(storeSlug);

        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        access.checkAdminAccess(userId, store.getId());

        if (!p.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Produto não pertence à loja");
        }

        if (req.getPromotionalPrice() != null &&
                req.getPromotionalPrice().compareTo(req.getPrice()) >= 0) {
            throw new RuntimeException("Preço promocional inválido");
        }

        if (!p.getSlug().equals(req.getSlug())) {
            productRepository.findByStoreIdAndSlug(store.getId(), req.getSlug())
                    .ifPresent(prod -> { throw new RuntimeException("Slug já existe"); });
        }

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        p.setName(req.getName());
        p.setSlug(req.getSlug());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setPromotionalPrice(req.getPromotionalPrice());
        p.setCategory(category);
        p.setVisible(req.getVisible());
        p.setUpdatedAt(LocalDateTime.now());
        p.setUpdatedBy(userId);

        return map(productRepository.save(p));
    }

    private Store getStoreBySlug(String slug) {
        return storeRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Loja não encontrada"));
    }

    private ProductResponse map(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .description(p.getDescription())
                .price(p.getPrice())
                .promotionalPrice(p.getPromotionalPrice())
                .visible(p.getVisible())
                .createdAt(p.getCreatedAt())
                .categoryId(p.getCategory().getId())
                .build();
    }
}