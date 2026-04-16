package com.catalog.controller.viewer;


import com.catalog.dto.category.CategoryResponse;
import com.catalog.dto.product.ProductResponse;
import com.catalog.service.CategoryService;
import com.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores/{storeSlug}/categories")
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> listCategories(
            @PathVariable String storeSlug) {

        return ResponseEntity.ok(categoryService.listPublicByStore(storeSlug));
    }

    @GetMapping("/{categorySlug}/products")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @PathVariable String storeSlug,
            @PathVariable String categorySlug,
            Pageable pageable) {

        return ResponseEntity.ok(
                productService.listByCategory(storeSlug, categorySlug, pageable)
        );
    }
}