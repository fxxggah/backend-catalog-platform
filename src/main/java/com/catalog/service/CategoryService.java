package com.catalog.service;

import com.catalog.domain.entity.Category;
import com.catalog.domain.entity.Store;
import com.catalog.dto.category.CategoryRequest;
import com.catalog.dto.category.CategoryResponse;
import com.catalog.repository.CategoryRepository;
import com.catalog.repository.StoreRepository;
import com.catalog.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final AccessControlService access;

    public CategoryResponse create(String storeSlug, CategoryRequest req, Long userId) {

        Store store = getStoreBySlug(storeSlug);

        access.checkAdminAccess(userId, store.getId());

        String generatedSlug = generateUniqueCategorySlug(store.getId(), req.getName());

        Category c = new Category();
        c.setName(req.getName());
        c.setSlug(generatedSlug);
        c.setStore(store);
        c.setCreatedAt(LocalDateTime.now());
        c.setCreatedBy(userId);

        return map(categoryRepository.save(c));
    }

    public List<CategoryResponse> listByStore(String storeSlug, Long userId) {

        Store store = getStoreBySlug(storeSlug);

        access.checkAdminAccess(userId, store.getId());

        return categoryRepository.findByStoreId(store.getId())
                .stream()
                .filter(c -> c.getDeletedAt() == null)
                .map(this::map)
                .toList();
    }

    public List<CategoryResponse> listPublicByStore(String storeSlug) {

        Store store = getStoreBySlug(storeSlug);

        return categoryRepository.findByStoreIdAndDeletedAtIsNull(store.getId())
                .stream()
                .filter(c -> c.getDeletedAt() == null)
                .map(this::map)
                .toList();
    }

    public CategoryResponse update(String storeSlug, Long id, CategoryRequest req, Long userId) {

        Store store = getStoreBySlug(storeSlug);

        access.checkAdminAccess(userId, store.getId());

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (!category.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Categoria não pertence à loja");
        }

        category.setName(req.getName());

        String generatedSlug = generateUniqueCategorySlugForUpdate(
                store.getId(),
                req.getName(),
                category.getId()
        );

        category.setSlug(generatedSlug);
        category.setUpdatedAt(LocalDateTime.now());
        category.setUpdatedBy(userId);

        return map(categoryRepository.save(category));
    }

    public void delete(String storeSlug, Long id, Long userId) {

        Store store = getStoreBySlug(storeSlug);

        access.checkAdminAccess(userId, store.getId());

        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (!c.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Categoria não pertence à loja");
        }

        c.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(c);
    }

    private String generateUniqueCategorySlugForUpdate(Long storeId, String name, Long currentCategoryId) {
        String baseSlug = SlugUtils.toSlug(name);
        String slug = baseSlug;
        int counter = 2;

        while (true) {
            Optional<Category> existing = categoryRepository.findByStoreIdAndSlug(storeId, slug);

            if (existing.isEmpty() || existing.get().getId().equals(currentCategoryId)) {
                return slug;
            }

            slug = baseSlug + "-" + counter;
            counter++;
        }
    }

    private String generateUniqueCategorySlug(Long storeId, String name) {
        String baseSlug = SlugUtils.toSlug(name);
        String slug = baseSlug;
        int counter = 2;

        while (categoryRepository.existsByStoreIdAndSlug(storeId, slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    private Store getStoreBySlug(String storeSlug) {
        return storeRepository.findBySlug(storeSlug)
                .orElseThrow(() -> new RuntimeException("Loja não encontrada"));
    }

    private CategoryResponse map(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .storeId(c.getStore().getId())
                .build();
    }
}