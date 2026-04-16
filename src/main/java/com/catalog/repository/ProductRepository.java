package com.catalog.repository;

import com.catalog.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByStoreId(Long storeId, Pageable pageable);

    Page<Product> findByStoreIdAndVisibleTrue(Long storeId, Pageable pageable);

    Optional<Product> findByStoreIdAndSlug(Long storeId, String slug);

    Page<Product> findByStoreIdAndVisibleTrueAndDeletedAtIsNull(Long storeId, Pageable pageable);

    Page<Product> findByStoreIdAndCategoryIdAndVisibleTrueAndDeletedAtIsNull(Long storeId, Long categoryId, Pageable pageable);

    Optional<Product> findByStoreIdAndSlugAndDeletedAtIsNull(Long storeId, String slug);

    Page<Product> findByStoreIdAndNameContainingIgnoreCaseAndDeletedAtIsNull(Long storeId, String name, Pageable pageable);

}