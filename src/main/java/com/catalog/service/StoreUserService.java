package com.catalog.service;

import com.catalog.domain.entity.Store;
import com.catalog.domain.entity.StoreUser;
import com.catalog.domain.enums.Role;
import com.catalog.dto.storeuser.StoreUserResponse;
import com.catalog.exception.BadRequestException;
import com.catalog.exception.ErrorCode;
import com.catalog.exception.NotFoundException;
import com.catalog.repository.StoreRepository;
import com.catalog.repository.StoreUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreUserService {

    private final StoreUserRepository repo;
    private final StoreRepository storeRepository;
    private final AccessControlService access;

    @Transactional(readOnly = true)
    public StoreUserResponse getCurrentUserInStore(String storeSlug, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        access.checkAdminAccess(userId, store.getId());

        StoreUser storeUser = repo.findByUserIdAndStoreIdWithUserAndStore(userId, store.getId())
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.USER_NOT_FOUND,
                        "Usuário não pertence à loja."
                ));

        return toResponse(storeUser);
    }

    @Transactional(readOnly = true)
    public List<StoreUserResponse> listByStore(String storeSlug, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        access.checkAdminAccess(userId, store.getId());

        return repo.findByStoreIdWithUserAndStore(store.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void removeUser(String storeSlug, Long userIdToRemove, Long ownerId) {
        Store store = getStoreBySlug(storeSlug);

        access.checkOwnerAccess(ownerId, store.getId());

        StoreUser storeUserToRemove = repo.findByUserIdAndStoreId(userIdToRemove, store.getId())
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.USER_NOT_FOUND,
                        "Usuário não encontrado na loja."
                ));

        if (storeUserToRemove.getRole() == Role.OWNER) {
            throw new BadRequestException(
                    ErrorCode.INVALID_OPERATION,
                    "Não é possível remover o OWNER da loja."
            );
        }

        if (userIdToRemove.equals(ownerId)) {
            throw new BadRequestException(
                    ErrorCode.INVALID_OPERATION,
                    "Você não pode sair da própria loja. Desative a loja se necessário."
            );
        }

        repo.delete(storeUserToRemove);
    }

    private Store getStoreBySlug(String slug) {
        return storeRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.STORE_NOT_FOUND,
                        "Loja não encontrada."
                ));
    }

    private StoreUserResponse toResponse(StoreUser storeUser) {
        return StoreUserResponse.builder()
                .id(storeUser.getId())
                .userId(storeUser.getUser().getId())
                .storeId(storeUser.getStore().getId())
                .name(storeUser.getUser().getName())
                .email(storeUser.getUser().getEmail())
                .role(storeUser.getRole())
                .createdAt(storeUser.getCreatedAt())
                .build();
    }
}