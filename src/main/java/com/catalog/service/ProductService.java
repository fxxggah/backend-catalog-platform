package com.catalog.service;

import com.catalog.domain.entity.Category;
import com.catalog.domain.entity.Product;
import com.catalog.domain.entity.ProductImage;
import com.catalog.domain.entity.Store;
import com.catalog.dto.product.ProductRequest;
import com.catalog.dto.product.ProductResponse;
import com.catalog.dto.productimage.ProductImageResponse;
import com.catalog.repository.CategoryRepository;
import com.catalog.repository.ProductRepository;
import com.catalog.repository.StoreRepository;
import com.catalog.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int DEFAULT_RELATED_PRODUCTS_LIMIT = 10;
    private static final int MAX_RELATED_PRODUCTS_LIMIT = 10;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final AccessControlService access;

    public ProductResponse create(String storeSlug, ProductRequest req, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        access.checkAdminAccess(userId, store.getId());

        validatePromotionalPrice(req);

        Category category = categoryRepository.findById(req.getCategoryId())
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (!category.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Categoria não pertence à loja");
        }

        boolean inStock = req.getInStock() != null ? req.getInStock() : true;
        boolean featured = Boolean.TRUE.equals(req.getFeatured());

        if (!inStock && featured) {
            throw new RuntimeException("Produto esgotado não pode ser marcado como destaque");
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
        product.setInStock(inStock);
        product.setFeatured(featured);
        product.setCreatedAt(LocalDateTime.now());
        product.setCreatedBy(userId);

        return map(productRepository.save(product));
    }

    public Page<ProductResponse> listAdmin(String storeSlug, String search, Pageable pageable, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        access.checkAdminAccess(userId, store.getId());

        Page<Product> page;

        if (search != null && !search.isBlank()) {
            page = productRepository.findByStoreIdAndNameContainingIgnoreCaseAndDeletedAtIsNull(
                    store.getId(), search, pageable
            );
        } else {
            page = productRepository.findByStoreIdAndDeletedAtIsNull(
                    store.getId(), pageable
            );
        }

        return page.map(this::map);
    }

    public Page<ProductResponse> listPublic(String storeSlug, String search, Pageable pageable) {
        Store store = getStoreBySlug(storeSlug);

        Page<Product> page;

        if (search != null && !search.isBlank()) {
            page = productRepository.findPublicByStoreIdAndNameOrderByInStockFirst(
                    store.getId(), search, pageable
            );
        } else {
            page = productRepository.findPublicByStoreIdOrderByInStockFirst(
                    store.getId(), pageable
            );
        }

        return page.map(this::map);
    }

    public Page<ProductResponse> listByCategory(String storeSlug, String categorySlug, Pageable pageable) {
        Store store = getStoreBySlug(storeSlug);

        Category category = categoryRepository
                .findByStoreIdAndSlugAndDeletedAtIsNull(store.getId(), categorySlug)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        Page<Product> page = productRepository.findPublicByStoreIdAndCategoryIdOrderByInStockFirst(
                store.getId(),
                category.getId(),
                pageable
        );

        return page.map(this::map);
    }

    public ProductResponse getBySlug(String storeSlug, String productSlug) {
        Store store = getStoreBySlug(storeSlug);

        Product product = productRepository
                .findByStoreIdAndSlugAndDeletedAtIsNull(store.getId(), productSlug)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        return map(product);
    }

    public List<ProductResponse> getRelatedProducts(String storeSlug, String productSlug, int limit) {
        Store store = getStoreBySlug(storeSlug);

        Product currentProduct = productRepository
                .findByStoreIdAndSlugAndDeletedAtIsNull(store.getId(), productSlug)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Long categoryId = currentProduct.getCategory().getId();
        int safeLimit = normalizeRelatedProductsLimit(limit);

        Pageable pageable = PageRequest.of(0, safeLimit);

        return productRepository
                .findByStoreIdAndCategoryIdAndIdNotAndInStockTrueAndDeletedAtIsNull(
                        store.getId(),
                        categoryId,
                        currentProduct.getId(),
                        pageable
                )
                .stream()
                .map(this::map)
                .toList();
    }

    public ProductResponse getById(String storeSlug, Long id, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        access.checkAdminAccess(userId, store.getId());

        Product product = productRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (!product.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Produto não pertence à loja");
        }

        return map(product);
    }

    public ProductResponse update(String storeSlug, Long id, ProductRequest req, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        access.checkAdminAccess(userId, store.getId());

        Product product = productRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (!product.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Produto não pertence à loja");
        }

        validatePromotionalPrice(req);

        Category category = categoryRepository.findById(req.getCategoryId())
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (!category.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Categoria não pertence à loja");
        }

        boolean inStock = req.getInStock() != null ? req.getInStock() : true;
        boolean featured = Boolean.TRUE.equals(req.getFeatured());

        if (!inStock && featured) {
            throw new RuntimeException("Produto esgotado não pode ser marcado como destaque");
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
        product.setInStock(inStock);
        product.setFeatured(featured);
        product.setUpdatedAt(LocalDateTime.now());
        product.setUpdatedBy(userId);

        return map(productRepository.save(product));
    }

    public void delete(String storeSlug, Long id, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        access.checkAdminAccess(userId, store.getId());

        Product product = productRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (!product.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Produto não pertence à loja");
        }

        product.setDeletedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setUpdatedBy(userId);

        productRepository.save(product);
    }

    public List<ProductResponse> getFeaturedProducts(String storeSlug) {
        Store store = getStoreBySlug(storeSlug);

        return productRepository
                .findTop8ByStoreIdAndFeaturedTrueAndInStockTrueAndDeletedAtIsNullOrderByCreatedAtDesc(
                        store.getId()
                )
                .stream()
                .map(this::map)
                .toList();
    }

    public List<ProductResponse> getNewArrivals(String storeSlug) {
        Store store = getStoreBySlug(storeSlug);

        return productRepository
                .findTop8ByStoreIdAndInStockTrueAndDeletedAtIsNullOrderByCreatedAtDesc(
                        store.getId()
                )
                .stream()
                .map(this::map)
                .toList();
    }

    private int normalizeRelatedProductsLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_RELATED_PRODUCTS_LIMIT;
        }

        return Math.min(limit, MAX_RELATED_PRODUCTS_LIMIT);
    }

    private void validatePromotionalPrice(ProductRequest req) {
        if (req.getPromotionalPrice() != null &&
                req.getPromotionalPrice().compareTo(req.getPrice()) >= 0) {
            throw new RuntimeException("Preço promocional inválido");
        }
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

    private Store getStoreBySlug(String storeSlug) {
        return storeRepository.findBySlug(storeSlug)
                .orElseThrow(() -> new RuntimeException("Loja não encontrada"));
    }

    private ProductResponse map(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .promotionalPrice(product.getPromotionalPrice())
                .inStock(product.getInStock())
                .featured(product.getFeatured())
                .createdAt(product.getCreatedAt())
                .categoryId(product.getCategory().getId())
                .images(
                        product.getImages() == null
                                ? List.of()
                                : product.getImages().stream()
                                .sorted(Comparator.comparing(ProductImage::getPosition))
                                .map(img -> ProductImageResponse.builder()
                                        .id(img.getId())
                                        .imageUrl(img.getImageUrl())
                                        .position(img.getPosition())
                                        .build())
                                .toList()
                )
                .build();
    }
}