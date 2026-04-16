package com.catalog.domain.entity;

import com.catalog.domain.enums.Role;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "store_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_store_user", columnNames = {"store_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_store_user_user", columnList = "user_id"),
                @Index(name = "idx_store_user_store", columnList = "store_id")
        }
)
@Getter
@Setter
public class StoreUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}