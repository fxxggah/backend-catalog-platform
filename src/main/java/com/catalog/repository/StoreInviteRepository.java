package com.catalog.repository;

import com.catalog.domain.entity.StoreInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreInviteRepository extends JpaRepository<StoreInvite, Long> {

    Optional<StoreInvite> findByToken(String token);

    List<StoreInvite> findByStoreIdAndUsedAtIsNull(Long storeId);

    Optional<StoreInvite> findByTokenAndUsedAtIsNull(String token);

    Optional<StoreInvite> findByTokenAndUsedAtIsNullAndExpiresAtAfter(String token, LocalDateTime now);

    boolean existsByEmailAndStoreIdAndUsedAtIsNull(String email, Long storeId);

}