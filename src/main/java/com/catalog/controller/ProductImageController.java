package com.catalog.controller;

import com.catalog.dto.productimage.ProductImageResponse;
import com.catalog.dto.productimage.UploadImageRequest;
import com.catalog.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/product-images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService service;

    @PostMapping
    public ProductImageResponse upload(@RequestParam Long productId, @RequestParam MultipartFile file, @RequestHeader("userId") Long userId) {

        UploadImageRequest req = new UploadImageRequest();
        req.setProductId(productId);
        req.setFile(file);

        return service.upload(req, userId);
    }
}