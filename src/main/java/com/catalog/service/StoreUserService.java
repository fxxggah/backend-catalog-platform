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

    public StoreUserResponse getCurrentUserInStore(String storeSlug, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        StoreUser storeUser = access.getStoreUserOrThrow(userId, store.getId());

        return toResponse(storeUser);
    }

    public List<StoreUserResponse> listByStore(String storeSlug, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        access.checkAdminAccess(userId, store.getId());

        return repo.findByStoreId(store.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void removeUser(String storeSlug, Long userIdToRemove, Long ownerId) {
        Store store = getStoreBySlug(storeSlug);

        access.checkOwnerAccess(ownerId, store.getId());

        StoreUser storeUserToRemove = repo.findByUserIdAndStoreId(userIdToRemove, store.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (storeUserToRemove.getRole() == Role.OWNER) {
            throw new RuntimeException("Não pode remover o OWNER");
        }

        if (userIdToRemove.equals(ownerId)) {
            throw new RuntimeException("Você não pode sair da própria loja, apenas desative a loja");
        }

        repo.delete(storeUserToRemove);
    }

    private Store getStoreBySlug(String slug) {
        return storeRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Loja não encontrada"));
    }

    private StoreUserResponse toResponse(StoreUser storeUser) {
        return StoreUserResponse.builder()
                .id(storeUser.getId())
                .userId(storeUser.getUser().getId())
                .storeId(storeUser.getStore().getId())
                .name(storeUser.getUser().getName())
                .email(storeUser.getUser().getEmail())
                .pictureUrl(storeUser.getUser().getPictureUrl())
                .role(storeUser.getRole())
                .createdAt(storeUser.getCreatedAt())
                .build();
    }
}