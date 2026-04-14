package com.catalog.domain.entity;

import com.catalog.domain.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class StoreUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Store store;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime createdAt;
}