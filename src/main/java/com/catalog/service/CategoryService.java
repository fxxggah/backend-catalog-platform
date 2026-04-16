package com.catalog.service;

import com.catalog.domain.entity.Category;
import com.catalog.domain.entity.Store;
import com.catalog.dto.category.CategoryRequest;
import com.catalog.dto.category.CategoryResponse;
import com.catalog.repository.CategoryRepository;
import com.catalog.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final AccessControlService access;

    public CategoryResponse create(String storeSlug, CategoryRequest req, Long userId) {

        Store store = getStoreBySlug(storeSlug);

        access.checkAdminAccess(userId, store.getId());

        categoryRepository.findByStoreIdAndSlug(store.getId(), req.getSlug())
                .ifPresent(c -> { throw new RuntimeException("Slug já existe"); });

        Category c = new Category();
        c.setName(req.getName());
        c.setSlug(req.getSlug());
        c.setStore(store);
        c.setCreatedAt(LocalDateTime.now());
        c.setCreatedBy(userId);

        return map(categoryRepository.save(c));
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

        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        access.checkAdminAccess(userId, store.getId());

        if (!c.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Categoria não pertence à loja");
        }

        if (!c.getSlug().equals(req.getSlug())) {
            categoryRepository.findByStoreIdAndSlug(store.getId(), req.getSlug())
                    .ifPresent(cat -> { throw new RuntimeException("Slug já existe"); });
        }

        c.setName(req.getName());
        c.setSlug(req.getSlug());
        c.setUpdatedAt(LocalDateTime.now());
        c.setUpdatedBy(userId);

        return map(categoryRepository.save(c));
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