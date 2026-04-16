package com.catalog.controller.admin;

import com.catalog.service.auth.AuthContextService;
import com.catalog.dto.product.ProductResponse;
import com.catalog.dto.product.ProductRequest;
import com.catalog.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/stores/{storeSlug}/products")
@RequiredArgsConstructor
@Validated
public class AdminProductController {

    private final ProductService productService;
    private final AuthContextService authContextService;

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @PathVariable String storeSlug,
            @Valid @RequestBody ProductRequest request,
            @RequestHeader("userId") Long userId) {

        return ResponseEntity.status(201)
                .body(productService.create(storeSlug, request, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable String storeSlug,
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @RequestHeader("userId") Long userId) {

        return ResponseEntity.ok(
                productService.update(storeSlug, id, request, userId)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String storeSlug,
            @PathVariable Long id,
            @RequestHeader("userId") Long userId) {

        productService.delete(storeSlug, id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> list(
            @PathVariable String storeSlug,
            @RequestParam(required = false) String search,
            Pageable pageable,
            @RequestHeader("userId") Long userId) {

        return ResponseEntity.ok(
                productService.list(storeSlug, search, pageable)
        );
    }
}