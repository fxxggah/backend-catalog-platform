package com.catalog.service;

import com.catalog.domain.entity.Category;
import com.catalog.domain.entity.Store;
import com.catalog.dto.category.CategoryRequest;
import com.catalog.dto.category.CategoryResponse;
import com.catalog.domain.enums.ErrorCode;
import com.catalog.exception.ForbiddenException;
import com.catalog.exception.NotFoundException;
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

        Category category = new Category();
        category.setName(req.getName());
        category.setSlug(generatedSlug);
        category.setStore(store);
        category.setCreatedAt(LocalDateTime.now());
        category.setCreatedBy(userId);

        return map(categoryRepository.save(category));
    }

    public List<CategoryResponse> listByStore(String storeSlug, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        access.checkAdminAccess(userId, store.getId());

        return categoryRepository.findByStoreIdAndDeletedAtIsNull(store.getId())
                .stream()
                .map(this::map)
                .toList();
    }

    public List<CategoryResponse> listPublicByStore(String storeSlug) {
        Store store = getStoreBySlug(storeSlug);

        return categoryRepository.findByStoreIdAndDeletedAtIsNull(store.getId())
                .stream()
                .map(this::map)
                .toList();
    }

    public CategoryResponse update(String storeSlug, Long id, CategoryRequest req, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        access.checkAdminAccess(userId, store.getId());

        Category category = categoryRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.CATEGORY_NOT_FOUND,
                        "Categoria não encontrada."
                ));

        if (!category.getStore().getId().equals(store.getId())) {
            throw new ForbiddenException(
                    ErrorCode.RESOURCE_NOT_IN_STORE,
                    "Categoria não pertence à loja."
            );
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

        Category category = categoryRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.CATEGORY_NOT_FOUND,
                        "Categoria não encontrada."
                ));

        if (!category.getStore().getId().equals(store.getId())) {
            throw new ForbiddenException(
                    ErrorCode.RESOURCE_NOT_IN_STORE,
                    "Categoria não pertence à loja."
            );
        }

        category.setDeletedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category.setUpdatedBy(userId);

        categoryRepository.save(category);
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

    private Store getStoreBySlug(String storeSlug) {
        return storeRepository.findBySlug(storeSlug)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.STORE_NOT_FOUND,
                        "Loja não encontrada."
                ));
    }

    private CategoryResponse map(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .storeId(category.getStore().getId())
                .build();
    }
}