package com.catalog.service.auth;

import com.catalog.domain.enums.ErrorCode;
import com.catalog.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthContextService {

    public Long getUserId() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException(
                    ErrorCode.UNAUTHORIZED,
                    "Usuário não autenticado."
            );
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Long userId) {
            return userId;
        }

        if (principal instanceof String userId) {
            return Long.valueOf(userId);
        }

        throw new UnauthorizedException(
                ErrorCode.UNAUTHORIZED,
                "Usuário autenticado inválido."
        );
    }
}