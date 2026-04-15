package com.catalog.service;

import com.catalog.domain.entity.StoreUser;
import com.catalog.domain.enums.Role;
import com.catalog.dto.storeuser.StoreUserResponse;
import com.catalog.repository.StoreUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreUserService {

    private final StoreUserRepository repo;
    private final AccessControlService access;

    public List<StoreUserResponse> listByStore(Long storeId, Long userId) {
        access.checkAdminAccess(userId, storeId);

        return repo.findByStoreId(storeId)
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

    public void removeUser(Long storeId, Long userIdToRemove, Long ownerId) {

        access.checkOwnerAccess(ownerId, storeId);

        StoreUser su = repo.findByUserIdAndStoreId(userIdToRemove, storeId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (su.getRole() == Role.OWNER) {
            throw new RuntimeException("Não pode remover o OWNER");
        }

        if (userIdToRemove.equals(ownerId)) {
            throw new RuntimeException("Você não pode sair da própria loja");
        }

        repo.delete(su);
    }
}
