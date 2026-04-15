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

    public ProductResponse create(ProductRequest req, Long userId) {

        access.checkAdminAccess(userId, req.getStoreId());

        if (req.getPromotionalPrice() != null &&
                req.getPromotionalPrice().compareTo(req.getPrice()) >= 0) {
            throw new RuntimeException("Preço promocional inválido");
        }

        productRepository.findByStoreIdAndSlug(req.getStoreId(), req.getSlug())
                .ifPresent(p -> { throw new RuntimeException("Slug já existe"); });

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        Store store = storeRepository.findById(req.getStoreId())
                .orElseThrow();

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

    public Page<ProductResponse> list(Long storeId, String search, Pageable pageable) {
        Page<Product> page;

        if (search != null && !search.isBlank()) {
            page = productRepository
                    .findByStoreIdAndNameContainingIgnoreCaseAndDeletedAtIsNull(storeId, search, pageable);
        } else {
            page = productRepository
                    .findByStoreIdAndVisibleTrueAndDeletedAtIsNull(storeId, pageable);
        }

        return page.map(this::map);
    }

    public void delete(Long id, Long userId, Long storeId) {
        access.checkAdminAccess(userId, storeId);

        Product p = productRepository.findById(id).orElseThrow();
        p.setDeletedAt(LocalDateTime.now());

        productRepository.save(p);
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

    public ProductResponse getById(Long id, Long userId) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        access.checkAdminAccess(userId, p.getStore().getId());

        return map(p);
    }

    public ProductResponse getBySlug(Long storeId, String slug) {
        Product p = productRepository.findByStoreIdAndSlug(storeId, slug)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (p.getDeletedAt() != null || !p.getVisible()) {
            throw new RuntimeException("Produto não disponível");
        }

        return map(p);
    }

    public ProductResponse update(Long id, ProductRequest req, Long userId) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        access.checkAdminAccess(userId, p.getStore().getId());

        if (req.getPromotionalPrice() != null &&
                req.getPromotionalPrice().compareTo(req.getPrice()) >= 0) {
            throw new RuntimeException("Preço promocional inválido");
        }

        if (!p.getSlug().equals(req.getSlug())) {
            productRepository.findByStoreIdAndSlug(p.getStore().getId(), req.getSlug())
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

    public Page<ProductResponse> listAll(Long storeId, Pageable pageable, Long userId) {
        access.checkAdminAccess(userId, storeId);

        return productRepository.findByStoreIdAndVisibleTrue(storeId, pageable)
                .map(this::map);
    }
}