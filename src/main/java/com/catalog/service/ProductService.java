package com.catalog.service;

import com.catalog.domain.entity.Product;
import com.catalog.dto.product.ProductRequest;
import com.catalog.dto.product.ProductResponse;
import com.catalog.repository.ProductRepository;
import com.catalog.util.SlugUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.catalog.domain.entity.Category;
import com.catalog.domain.entity.Store;
import com.catalog.repository.CategoryRepository;
import com.catalog.repository.StoreRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

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

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (!category.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Categoria não pertence à loja");
        }

        String generatedSlug = generateUniqueProductSlug(store.getId(), req.getName());

        Product product = new Product();
        product.setName(req.getName());
        product.setSlug(generatedSlug);
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setPromotionalPrice(req.getPromotionalPrice());
        product.setCategory(category);
        product.setStore(store);
        product.setVisible(req.getVisible() != null ? req.getVisible() : true);
        product.setCreatedAt(LocalDateTime.now());
        product.setCreatedBy(userId);

        return map(productRepository.save(product));
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

        access.checkAdminAccess(userId, store.getId());

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (!product.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Produto não pertence à loja");
        }

        if (req.getPromotionalPrice() != null &&
                req.getPromotionalPrice().compareTo(req.getPrice()) >= 0) {
            throw new RuntimeException("Preço promocional inválido");
        }

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (!category.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Categoria não pertence à loja");
        }

        product.setName(req.getName());

        String generatedSlug = generateUniqueProductSlugForUpdate(
                store.getId(),
                req.getName(),
                product.getId()
        );

        product.setSlug(generatedSlug);
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setPromotionalPrice(req.getPromotionalPrice());
        product.setCategory(category);
        product.setVisible(req.getVisible() != null ? req.getVisible() : true);
        product.setUpdatedAt(LocalDateTime.now());
        product.setUpdatedBy(userId);

        return map(productRepository.save(product));
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

    private String generateUniqueProductSlug(Long storeId, String name) {
        String baseSlug = SlugUtils.toSlug(name);
        String slug = baseSlug;
        int counter = 2;

        while (productRepository.existsByStoreIdAndSlug(storeId, slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    private String generateUniqueProductSlugForUpdate(Long storeId, String name, Long currentProductId) {
        String baseSlug = SlugUtils.toSlug(name);
        String slug = baseSlug;
        int counter = 2;

        while (true) {
            Optional<Product> existing = productRepository.findByStoreIdAndSlug(storeId, slug);

            if (existing.isEmpty() || existing.get().getId().equals(currentProductId)) {
                return slug;
            }

            slug = baseSlug + "-" + counter;
            counter++;
        }
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