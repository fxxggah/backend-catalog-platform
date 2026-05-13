package com.catalog.repository;

import com.catalog.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            SELECT p FROM Product p
            WHERE p.store.id = :storeId
            AND p.deletedAt IS NULL
            ORDER BY p.inStock DESC, p.createdAt DESC
            """)
    Page<Product> findPublicByStoreIdOrderByInStockFirst(Long storeId, Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE p.store.id = :storeId
            AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
            AND p.deletedAt IS NULL
            ORDER BY p.inStock DESC, p.createdAt DESC
            """)
    Page<Product> findPublicByStoreIdAndNameOrderByInStockFirst(
            Long storeId,
            String name,
            Pageable pageable
    );

    @Query("""
            SELECT p FROM Product p
            WHERE p.store.id = :storeId
            AND p.category.id = :categoryId
            AND p.deletedAt IS NULL
            ORDER BY p.inStock DESC, p.createdAt DESC
            """)
    Page<Product> findPublicByStoreIdAndCategoryIdOrderByInStockFirst(
            Long storeId,
            Long categoryId,
            Pageable pageable
    );

    Page<Product> findByStoreIdAndDeletedAtIsNull(Long storeId, Pageable pageable);

    Page<Product> findByStoreIdAndNameContainingIgnoreCaseAndDeletedAtIsNull(
            Long storeId,
            String name,
            Pageable pageable
    );

    Optional<Product> findByStoreIdAndSlug(Long storeId, String slug);

    Optional<Product> findByStoreIdAndSlugAndDeletedAtIsNull(Long storeId, String slug);

    boolean existsByStoreIdAndSlug(Long storeId, String slug);

    List<Product> findTop8ByStoreIdAndFeaturedTrueAndInStockTrueAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long storeId
    );

    List<Product> findTop8ByStoreIdAndInStockTrueAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long storeId
    );

    List<Product> findByStoreIdAndCategoryIdAndIdNotAndInStockTrueAndDeletedAtIsNull(
            Long storeId,
            Long categoryId,
            Long productId,
            Pageable pageable
    );
}