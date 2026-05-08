package com.catalog.repository;

import com.catalog.domain.entity.AnalyticsEvent;
import com.catalog.domain.enums.AnalyticsEventType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    long countByStoreIdAndEventType(Long storeId, AnalyticsEventType eventType);

    long countByStoreIdAndEventTypeAndCreatedAtBetween(
            Long storeId,
            AnalyticsEventType eventType,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
            SELECT p.id, p.name, p.slug, COUNT(e.id)
            FROM AnalyticsEvent e
            JOIN e.product p
            WHERE e.store.id = :storeId
              AND e.eventType = :eventType
              AND p.deletedAt IS NULL
            GROUP BY p.id, p.name, p.slug
            ORDER BY COUNT(e.id) DESC
            """)
    List<Object[]> findTopProductsByEventType(
            @Param("storeId") Long storeId,
            @Param("eventType") AnalyticsEventType eventType,
            Pageable pageable
    );

    @Query("""
            SELECT DATE(e.createdAt), COUNT(e.id)
            FROM AnalyticsEvent e
            WHERE e.store.id = :storeId
              AND e.eventType = :eventType
              AND e.createdAt >= :start
            GROUP BY DATE(e.createdAt)
            ORDER BY DATE(e.createdAt) ASC
            """)
    List<Object[]> findDailyVisits(
            @Param("storeId") Long storeId,
            @Param("eventType") AnalyticsEventType eventType,
            @Param("start") LocalDateTime start
    );
}