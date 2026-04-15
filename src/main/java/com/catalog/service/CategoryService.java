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

    public CategoryResponse create(CategoryRequest req, Long userId) {

        access.checkAdminAccess(userId, req.getStoreId());

        categoryRepository.findByStoreIdAndSlug(req.getStoreId(), req.getSlug())
                .ifPresent(c -> { throw new RuntimeException("Slug já existe"); });

        Store store = storeRepository.findById(req.getStoreId())
                .orElseThrow(() -> new RuntimeException("Loja não encontrada"));

        Category c = new Category();
        c.setName(req.getName());
        c.setSlug(req.getSlug());
        c.setStore(store);
        c.setCreatedAt(LocalDateTime.now());
        c.setCreatedBy(userId);

        return map(categoryRepository.save(c));
    }

    public void delete(Long id, Long userId, Long storeId) {
        access.checkAdminAccess(userId, storeId);

        Category c = categoryRepository.findById(id)
                .orElseThrow();

        c.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(c);
    }

    private CategoryResponse map(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .storeId(c.getStore().getId())
                .build();
    }

    public List<CategoryResponse> listByStore(Long storeId, Long userId) {
        access.checkAdminAccess(userId, storeId);

        return categoryRepository.findByStoreId(storeId)
                .stream()
                .filter(c -> c.getDeletedAt() == null)
                .map(this::map)
                .toList();
    }

    public CategoryResponse getById(Long id, Long userId) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        access.checkAdminAccess(userId, c.getStore().getId());

        return map(c);
    }

    public CategoryResponse update(Long id, CategoryRequest req, Long userId) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        access.checkAdminAccess(userId, c.getStore().getId());

        if (!c.getSlug().equals(req.getSlug())) {
            categoryRepository.findByStoreIdAndSlug(c.getStore().getId(), req.getSlug())
                    .ifPresent(cat -> { throw new RuntimeException("Slug já existe"); });
        }

        c.setName(req.getName());
        c.setSlug(req.getSlug());
        c.setUpdatedAt(LocalDateTime.now());
        c.setUpdatedBy(userId);

        return map(categoryRepository.save(c));
    }

}