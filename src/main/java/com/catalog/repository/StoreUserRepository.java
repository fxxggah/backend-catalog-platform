package com.catalog.repository;

import com.catalog.domain.entity.StoreUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreUserRepository extends JpaRepository<StoreUser, Long> {
    Optional<StoreUser> findByUserIdAndStoreId(Long userId, Long storeId);
    List<StoreUser> findByStoreId(Long storeId);
}