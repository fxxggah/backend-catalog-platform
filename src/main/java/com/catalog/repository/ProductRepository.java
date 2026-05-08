package com.catalog.repository;

import com.catalog.domain.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByStoreIdAndDeletedAtIsNull(Long storeId, Pageable pageable);

    Page<Product> findByStoreIdAndVisibleTrueAndDeletedAtIsNull(Long storeId, Pageable pageable);

    Page<Product> findByStoreIdAndCategoryIdAndVisibleTrueAndDeletedAtIsNull(
            Long storeId,
            Long categoryId,
            Pageable pageable
    );

    List<Product> findByStoreIdAndCategoryIdAndIdNotAndVisibleTrueAndDeletedAtIsNull(
            Long storeId,
            Long categoryId,
            Long productId,
            Pageable pageable
    );

    Page<Product> findByStoreIdAndNameContainingIgnoreCaseAndDeletedAtIsNull(
            Long storeId,
            String name,
            Pageable pageable
    );

    Page<Product> findByStoreIdAndNameContainingIgnoreCaseAndVisibleTrueAndDeletedAtIsNull(
            Long storeId,
            String name,
            Pageable pageable
    );

    Optional<Product> findByStoreIdAndSlug(Long storeId, String slug);

    Optional<Product> findByStoreIdAndSlugAndDeletedAtIsNull(Long storeId, String slug);

    boolean existsByStoreIdAndSlug(Long storeId, String slug);

    List<Product> findTop8ByStoreIdAndFeaturedTrueAndVisibleTrueAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long storeId
    );

    List<Product> findTop8ByStoreIdAndVisibleTrueAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long storeId
    );
}