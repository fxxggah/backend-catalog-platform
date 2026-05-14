package com.catalog.service;

import com.catalog.domain.entity.Product;
import com.catalog.domain.entity.ProductImage;
import com.catalog.domain.entity.Store;
import com.catalog.dto.productimage.ProductImageReorderRequest;
import com.catalog.dto.productimage.ProductImageResponse;
import com.catalog.dto.productimage.UploadImageRequest;
import com.catalog.exception.BadRequestException;
import com.catalog.exception.ErrorCode;
import com.catalog.exception.ForbiddenException;
import com.catalog.exception.NotFoundException;
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

    private static final int MAX_IMAGES_PER_PRODUCT = 8;

    private final ProductImageRepository repo;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CloudinaryService cloudinaryService;
    private final AccessControlService access;

    public ProductImageResponse upload(String storeSlug, UploadImageRequest req, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.PRODUCT_NOT_FOUND,
                        "Produto não encontrado."
                ));

        access.checkAdminAccess(userId, store.getId());

        validateProductBelongsToStore(product, store.getId());

        List<ProductImage> images = repo.findByProductIdOrderByPositionAsc(product.getId());

        if (images.size() >= MAX_IMAGES_PER_PRODUCT) {
            throw new BadRequestException(
                    ErrorCode.IMAGE_LIMIT_REACHED,
                    "Limite de imagens atingido. O máximo permitido é 8 imagens por produto."
            );
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
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.PRODUCT_NOT_FOUND,
                        "Produto não encontrado."
                ));

        validateProductBelongsToStore(product, store.getId());

        return repo.findByProductIdOrderByPositionAsc(productId)
                .stream()
                .map(this::map)
                .toList();
    }

    public void reorder(
            String storeSlug,
            Long productId,
            ProductImageReorderRequest request,
            Long userId
    ) {
        Store store = getStoreBySlug(storeSlug);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.PRODUCT_NOT_FOUND,
                        "Produto não encontrado."
                ));

        access.checkAdminAccess(userId, store.getId());

        validateProductBelongsToStore(product, store.getId());

        List<ProductImage> images = repo.findByProductIdOrderByPositionAsc(productId);

        if (request.getImageIds() == null || images.size() != request.getImageIds().size()) {
            throw new BadRequestException(
                    ErrorCode.INVALID_OPERATION,
                    "Lista inválida para reordenação."
            );
        }

        Map<Long, ProductImage> imageMap = images.stream()
                .collect(Collectors.toMap(ProductImage::getId, image -> image));

        for (int i = 0; i < request.getImageIds().size(); i++) {
            Long imageId = request.getImageIds().get(i);

            ProductImage image = imageMap.get(imageId);

            if (image == null) {
                throw new BadRequestException(
                        ErrorCode.INVALID_OPERATION,
                        "Imagem inválida para reordenação."
                );
            }

            image.setPosition(i + 1);
        }

        repo.saveAll(images);
    }

    public void delete(String storeSlug, Long imageId, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        ProductImage image = repo.findById(imageId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.IMAGE_NOT_FOUND,
                        "Imagem não encontrada."
                ));

        Product product = image.getProduct();

        access.checkAdminAccess(userId, store.getId());

        validateProductBelongsToStore(product, store.getId());

        repo.delete(image);

        List<ProductImage> images = repo.findByProductIdOrderByPositionAsc(product.getId());

        for (int i = 0; i < images.size(); i++) {
            images.get(i).setPosition(i + 1);
        }

        repo.saveAll(images);
    }

    private Store getStoreBySlug(String slug) {
        return storeRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.STORE_NOT_FOUND,
                        "Loja não encontrada."
                ));
    }

    private void validateProductBelongsToStore(Product product, Long storeId) {
        if (!product.getStore().getId().equals(storeId)) {
            throw new ForbiddenException(
                    ErrorCode.RESOURCE_NOT_IN_STORE,
                    "Produto não pertence à loja."
            );
        }
    }

    private ProductImageResponse map(ProductImage image) {
        return ProductImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .position(image.getPosition())
                .build();
    }
}