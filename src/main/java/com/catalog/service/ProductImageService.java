package com.catalog.service;

import com.catalog.domain.entity.Product;
import com.catalog.domain.entity.ProductImage;
import com.catalog.domain.entity.Store;
import com.catalog.dto.productimage.ProductImageReorderRequest;
import com.catalog.dto.productimage.ProductImageResponse;
import com.catalog.dto.productimage.UploadImageRequest;
import com.catalog.repository.ProductImageRepository;
import com.catalog.repository.ProductRepository;
import com.catalog.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductImageRepository repo;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CloudinaryService cloudinaryService;
    private final AccessControlService access;

    public ProductImageResponse upload(String storeSlug, UploadImageRequest req, Long userId) {

        Store store = getStoreBySlug(storeSlug);

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        access.checkAdminAccess(userId, store.getId());

        if (!product.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Produto não pertence à loja");
        }

        List<ProductImage> images = repo.findByProductIdOrderByPositionAsc(product.getId());

        if (images.size() >= 8) {
            throw new RuntimeException("Limite de imagens atingido");
        }

        String imageUrl = cloudinaryService.uploadImage(req.getFile());

        ProductImage img = new ProductImage();
        img.setProduct(product);
        img.setImageUrl(imageUrl);
        img.setPosition(images.size() + 1);

        return map(repo.save(img));
    }

    public List<ProductImageResponse> getByProduct(String storeSlug, Long productId) {

        Store store = getStoreBySlug(storeSlug);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (!product.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Produto não pertence à loja");
        }

        return repo.findByProductIdOrderByPositionAsc(productId)
                .stream()
                .map(this::map)
                .toList();
    }

    public void reorder(String storeSlug, ProductImageReorderRequest request, Long userId) {

        Store store = getStoreBySlug(storeSlug);

        List<ProductImage> images = repo.findByProductIdOrderByPositionAsc(request.getProductId());

        if (images.isEmpty()) {
            throw new RuntimeException("Produto não possui imagens");
        }

        Product product = images.get(0).getProduct();

        access.checkAdminAccess(userId, store.getId());

        if (!product.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Produto não pertence à loja");
        }

        if (images.size() != request.getImageIds().size()) {
            throw new RuntimeException("Lista inválida para reordenação");
        }

        Map<Long, ProductImage> map = images.stream()
                .collect(Collectors.toMap(ProductImage::getId, img -> img));

        for (int i = 0; i < request.getImageIds().size(); i++) {

            Long imageId = request.getImageIds().get(i);

            ProductImage img = map.get(imageId);

            if (img == null) {
                throw new RuntimeException("Imagem inválida");
            }

            img.setPosition(i + 1);
        }

        repo.saveAll(images);
    }

    public void delete(String storeSlug, Long imageId, Long userId) {

        Store store = getStoreBySlug(storeSlug);

        ProductImage image = repo.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Imagem não encontrada"));

        Product product = image.getProduct();

        access.checkAdminAccess(userId, store.getId());

        if (!product.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Imagem não pertence à loja");
        }

        repo.delete(image);

        List<ProductImage> images = repo.findByProductIdOrderByPositionAsc(product.getId());

        for (int i = 0; i < images.size(); i++) {
            images.get(i).setPosition(i + 1);
        }

        repo.saveAll(images);
    }

    private Store getStoreBySlug(String slug) {
        return storeRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Loja não encontrada"));
    }

    private ProductImageResponse map(ProductImage i) {
        return ProductImageResponse.builder()
                .id(i.getId())
                .imageUrl(i.getImageUrl())
                .position(i.getPosition())
                .build();
    }
}