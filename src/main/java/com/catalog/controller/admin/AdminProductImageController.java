package com.catalog.controller.admin;


import com.catalog.service.auth.AuthContextService;
import com.catalog.dto.productimage.ProductImageReorderRequest;
import com.catalog.dto.productimage.ProductImageResponse;
import com.catalog.dto.productimage.UploadImageRequest;
import com.catalog.service.ProductImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/stores/{storeSlug}/products/{productId}/images")
@RequiredArgsConstructor
@Validated
public class AdminProductImageController {

    private final ProductImageService productImageService;
    private final AuthContextService authContextService;

    @PostMapping
    public ResponseEntity<ProductImageResponse> upload(
            @PathVariable String storeSlug,
            @PathVariable Long productId,
            @Valid @ModelAttribute UploadImageRequest request,
            @RequestHeader("userId") Long userId) {

        request.setProductId(productId);

        return ResponseEntity.status(201)
                .body(productImageService.upload(storeSlug, request, userId));
    }

    @GetMapping
    public ResponseEntity<List<ProductImageResponse>> getByProduct(
            @PathVariable String storeSlug,
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                productImageService.getByProduct(storeSlug, productId)
        );
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> delete(
            @PathVariable String storeSlug,
            @PathVariable Long imageId,
            @RequestHeader("userId") Long userId) {

        productImageService.delete(storeSlug, imageId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorder(
            @PathVariable String storeSlug,
            @Valid @RequestBody ProductImageReorderRequest request,
            @RequestHeader("userId") Long userId) {

        productImageService.reorder(storeSlug, request, userId);
        return ResponseEntity.noContent().build();
    }
}