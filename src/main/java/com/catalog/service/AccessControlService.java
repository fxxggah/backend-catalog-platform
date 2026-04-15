package com.catalog.service;

import com.catalog.domain.entity.StoreUser;
import com.catalog.domain.enums.Role;
import com.catalog.repository.StoreUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final StoreUserRepository storeUserRepository;

    public StoreUser getStoreUserOrThrow(Long userId, Long storeId) {
        return storeUserRepository.findByUserIdAndStoreId(userId, storeId)
                .orElseThrow(() -> new RuntimeException("Acesso negado à loja"));
    }

    public void checkOwnerAccess(Long userId, Long storeId) {
        StoreUser storeUser = getStoreUserOrThrow(userId, storeId);

        if (storeUser.getRole() != Role.OWNER) {
            throw new RuntimeException("Acesso negado: requer OWNER");
        }
    }

    public void checkAdminAccess(Long userId, Long storeId) {
        StoreUser storeUser = getStoreUserOrThrow(userId, storeId);

        if (storeUser.getRole() != Role.ADMIN && storeUser.getRole() != Role.OWNER) {
            throw new RuntimeException("Acesso negado: requer ADMIN ou OWNER");
        }
    }

}