package com.catalog.repository;

import com.catalog.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByStoreId(Long storeId);

    Optional<Category> findByStoreIdAndSlug(Long storeId, String slug);

    List<Category> findByStoreIdAndDeletedAtIsNull(Long storeId);

    Optional<Category> findByStoreIdAndSlugAndDeletedAtIsNull(Long storeId, String slug);

}