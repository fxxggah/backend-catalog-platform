package com.catalog.service;

import com.catalog.domain.entity.Store;
import com.catalog.domain.entity.StoreInvite;
import com.catalog.domain.entity.StoreUser;
import com.catalog.domain.entity.User;
import com.catalog.domain.enums.Role;
import com.catalog.dto.storeinvite.StoreInviteRequest;
import com.catalog.dto.storeinvite.StoreInviteResponse;
import com.catalog.repository.StoreInviteRepository;
import com.catalog.repository.StoreRepository;
import com.catalog.repository.StoreUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreInviteService {

    private final StoreInviteRepository repo;
    private final StoreRepository storeRepository;
    private final StoreUserRepository storeUserRepository;
    private final AccessControlService access;

    public StoreInviteResponse invite(StoreInviteRequest req, Long userId) {

        access.checkOwnerAccess(userId, req.getStoreId());

        storeUserRepository.findByUserIdAndStoreId(userId, req.getStoreId())
                .ifPresent(u -> { throw new RuntimeException("Esse usuario ja é admin da loja"); });

        Store store = storeRepository.findById(req.getStoreId()).orElseThrow();

        StoreInvite invite = new StoreInvite();
        invite.setStore(store);
        invite.setEmail(req.getEmail());
        invite.setToken(UUID.randomUUID().toString());
        invite.setExpiresAt(LocalDateTime.now().plusHours(24));
        invite.setCreatedAt(LocalDateTime.now());

        return map(repo.save(invite));
    }

    public void accept(String token, User user) {

        StoreInvite invite = repo
                .findByTokenAndUsedAtIsNullAndExpiresAtAfter(token, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Convite inválido"));

        StoreUser su = new StoreUser();
        su.setStore(invite.getStore());
        su.setUser(user);
        su.setRole(Role.ADMIN);
        su.setCreatedAt(LocalDateTime.now());

        storeUserRepository.save(su);

        invite.setUsedAt(LocalDateTime.now());
        repo.save(invite);
    }

    private StoreInviteResponse map(StoreInvite i) {
        return StoreInviteResponse.builder()
                .id(i.getId())
                .email(i.getEmail())
                .expiresAt(i.getExpiresAt())
                .usedAt(i.getUsedAt())
                .build();
    }

    public List<StoreInviteResponse> listByStore(Long storeId, Long userId) {
        access.checkOwnerAccess(userId, storeId);

        return repo.findByStoreIdAndUsedAtIsNull(storeId)
                .stream()
                .map(this::map)
                .toList();
    }

    public void cancel(Long inviteId, Long userId) {
        StoreInvite invite = repo.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Convite não encontrado"));

        access.checkOwnerAccess(userId, invite.getStore().getId());

        repo.delete(invite);
    }

    public StoreInviteResponse validateToken(String token) {
        StoreInvite invite = repo
                .findByTokenAndUsedAtIsNullAndExpiresAtAfter(token, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Convite inválido"));

        return map(invite);
    }

}