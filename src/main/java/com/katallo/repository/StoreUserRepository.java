package com.katallo.repository;

import com.katallo.domain.entity.StoreUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreUserRepository extends JpaRepository<StoreUser, Long> {

    Optional<StoreUser> findByUserIdAndStoreId(Long userId, Long storeId);

    List<StoreUser> findByStoreId(Long storeId);

    boolean existsByUserIdAndStoreId(Long userId, Long storeId);

    List<StoreUser> findByUserId(Long userId);

    @Query("""
            SELECT su FROM StoreUser su
            JOIN FETCH su.store
            WHERE su.user.id = :userId
            """)
    List<StoreUser> findByUserIdWithStore(Long userId);

    @Query("""
            SELECT su FROM StoreUser su
            JOIN FETCH su.user
            JOIN FETCH su.store
            WHERE su.store.id = :storeId
            """)
    List<StoreUser> findByStoreIdWithUserAndStore(Long storeId);

    @Query("""
            SELECT su FROM StoreUser su
            JOIN FETCH su.user
            JOIN FETCH su.store
            WHERE su.user.id = :userId
            AND su.store.id = :storeId
            """)
    Optional<StoreUser> findByUserIdAndStoreIdWithUserAndStore(Long userId, Long storeId);
}