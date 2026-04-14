package com.catalog.repository;

import com.catalog.domain.entity.StoreInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreInviteRepository extends JpaRepository<StoreInvite, Long> {
    Optional<StoreInvite> findByToken(String token);
    List<StoreInvite> findByStoreIdAndUsedAtIsNull(Long storeId);
}