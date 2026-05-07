package com.catalog.controller.viewer;

import com.catalog.dto.product.ProductResponse;
import com.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores/{storeSlug}/products")
@RequiredArgsConstructor
public class PublicProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> list(
            @PathVariable String storeSlug,
            @RequestParam(required = false) String search,
            Pageable pageable) {

        return ResponseEntity.ok(
                productService.listPublic(storeSlug, search, pageable)
        );
    }

    @GetMapping("/slug/{productSlug}")
    public ResponseEntity<ProductResponse> getBySlug(
            @PathVariable String storeSlug,
            @PathVariable String productSlug) {

        return ResponseEntity.ok(
                productService.getBySlug(storeSlug, productSlug)
        );
    }

    @GetMapping("/slug/{productSlug}/related")
    public ResponseEntity<List<ProductResponse>> getRelatedProducts(
            @PathVariable String storeSlug,
            @PathVariable String productSlug,
            @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(
                productService.getRelatedProducts(storeSlug, productSlug, limit)
        );
    }
}