package com.katallo.service;

import com.katallo.domain.entity.StoreUser;
import com.katallo.domain.enums.Role;
import com.katallo.domain.enums.ErrorCode;
import com.katallo.exception.ForbiddenException;
import com.katallo.repository.StoreUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final StoreUserRepository storeUserRepository;

    public StoreUser getStoreUserOrThrow(Long userId, Long storeId) {
        return storeUserRepository.findByUserIdAndStoreId(userId, storeId)
                .orElseThrow(() -> new ForbiddenException(
                        ErrorCode.ACCESS_DENIED,
                        "Acesso negado à loja."
                ));
    }

    public void checkOwnerAccess(Long userId, Long storeId) {
        StoreUser storeUser = getStoreUserOrThrow(userId, storeId);

        if (storeUser.getRole() != Role.OWNER) {
            throw new ForbiddenException(
                    ErrorCode.OWNER_REQUIRED,
                    "Acesso negado. Esta ação requer permissão de OWNER."
            );
        }
    }

    public void checkAdminAccess(Long userId, Long storeId) {
        StoreUser storeUser = getStoreUserOrThrow(userId, storeId);

        if (storeUser.getRole() != Role.ADMIN && storeUser.getRole() != Role.OWNER) {
            throw new ForbiddenException(
                    ErrorCode.ADMIN_REQUIRED,
                    "Acesso negado. Esta ação requer permissão de ADMIN ou OWNER."
            );
        }
    }
}