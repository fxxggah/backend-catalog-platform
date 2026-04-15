package com.catalog.service;

import com.catalog.domain.entity.Product;
import com.catalog.domain.entity.ProductImage;
import com.catalog.dto.productimage.ProductImageResponse;
import com.catalog.dto.productimage.UploadImageRequest;
import com.catalog.repository.ProductImageRepository;
import com.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductImageRepository repo;
    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;
    private final AccessControlService access;

    public ProductImageResponse upload(UploadImageRequest req, Long userId) {

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        access.checkAdminAccess(userId, product.getStore().getId());

        List<ProductImage> images = repo.findByProductIdOrderByPositionAsc(product.getId());

        if (images.size() >= 8) {
            throw new RuntimeException("Limite de imagens atingido");
        }

        String imageUrl = cloudinaryService.uploadImage(req.getFile());

        int position = images.size() + 1;

        ProductImage img = new ProductImage();
        img.setProduct(product);
        img.setImageUrl(imageUrl);
        img.setPosition(position);

        return map(repo.save(img));
    }

    private ProductImageResponse map(ProductImage i) {
        return ProductImageResponse.builder()
                .id(i.getId())
                .imageUrl(i.getImageUrl())
                .position(i.getPosition())
                .build();
    }
}