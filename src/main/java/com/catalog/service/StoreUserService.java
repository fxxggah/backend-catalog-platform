package com.catalog.service;

import com.catalog.domain.entity.Store;
import com.catalog.domain.entity.StoreUser;
import com.catalog.domain.enums.Role;
import com.catalog.dto.storeuser.StoreUserResponse;
import com.catalog.repository.StoreRepository;
import com.catalog.repository.StoreUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreUserService {

    private final StoreUserRepository repo;
    private final StoreRepository storeRepository;
    private final AccessControlService access;

    public List<StoreUserResponse> listByStore(String storeSlug, Long userId) {

        Store store = getStoreBySlug(storeSlug);

        access.checkOwnerAccess(userId, store.getId());

        return repo.findByStoreId(store.getId())
                .stream()
                .map(su -> StoreUserResponse.builder()
                        .id(su.getId())
                        .userId(su.getUser().getId())
                        .storeId(su.getStore().getId())
                        .role(su.getRole())
                        .createdAt(su.getCreatedAt())
                        .build())
                .toList();
    }

    public void removeUser(String storeSlug, Long userIdToRemove, Long ownerId) {

        Store store = getStoreBySlug(storeSlug);

        access.checkOwnerAccess(ownerId, store.getId());

        StoreUser su = repo.findByUserIdAndStoreId(userIdToRemove, store.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (su.getRole() == Role.OWNER) {
            throw new RuntimeException("Não pode remover o OWNER");
        }

        if (userIdToRemove.equals(ownerId)) {
            throw new RuntimeException("Você não pode sair da própria loja, apenas desative a loja");
        }

        repo.delete(su);
    }

    private Store getStoreBySlug(String slug) {
        return storeRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Loja não encontrada"));
    }
}