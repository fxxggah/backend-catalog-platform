package com.catalog.controller.admin;

import com.catalog.service.auth.AuthContextService;
import com.catalog.dto.category.CategoryResponse;
import com.catalog.dto.category.CategoryRequest;
import com.catalog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/stores/{storeSlug}/categories")
@RequiredArgsConstructor
@Validated
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final AuthContextService authContextService;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @PathVariable String storeSlug,
            @Valid @RequestBody CategoryRequest request,
            @RequestHeader("userId") Long userId) {

        return ResponseEntity.status(201)
                .body(categoryService.create(storeSlug, request, userId));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> list(
            @PathVariable String storeSlug,
            @RequestHeader("userId") Long userId) {

        return ResponseEntity.ok(
                categoryService.listByStore(storeSlug, userId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable String storeSlug,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            @RequestHeader("userId") Long userId) {

        return ResponseEntity.ok(
                categoryService.update(storeSlug, id, request, userId)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String storeSlug,
            @PathVariable Long id,
            @RequestHeader("userId") Long userId) {

        categoryService.delete(storeSlug, id, userId);
        return ResponseEntity.noContent().build();
    }
}