package com.catalog.service;

import com.catalog.domain.entity.Store;
import com.catalog.domain.entity.StoreInvite;
import com.catalog.domain.entity.StoreUser;
import com.catalog.domain.entity.User;
import com.catalog.domain.enums.Role;
import com.catalog.dto.storeinvite.StoreInviteCreateResponse;
import com.catalog.dto.storeinvite.StoreInviteRequest;
import com.catalog.dto.storeinvite.StoreInviteResponse;
import com.catalog.exception.BadRequestException;
import com.catalog.exception.ConflictException;
import com.catalog.domain.enums.ErrorCode;
import com.catalog.exception.ForbiddenException;
import com.catalog.exception.NotFoundException;
import com.catalog.repository.StoreInviteRepository;
import com.catalog.repository.StoreRepository;
import com.catalog.repository.StoreUserRepository;
import com.catalog.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final AccessControlService access;

    public StoreInviteCreateResponse invite(String storeSlug, StoreInviteRequest req, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        access.checkOwnerAccess(userId, store.getId());

        boolean alreadyInvited = repo.existsByEmailAndStoreIdAndUsedAtIsNull(
                req.getEmail(),
                store.getId()
        );

        if (alreadyInvited) {
            throw new ConflictException(
                    ErrorCode.INVALID_OPERATION,
                    "Já existe um convite pendente para este email."
            );
        }

        StoreInvite invite = new StoreInvite();
        invite.setStore(store);
        invite.setEmail(req.getEmail());
        invite.setToken(UUID.randomUUID().toString());
        invite.setCreatedBy(userId);
        invite.setExpiresAt(LocalDateTime.now().plusHours(24));
        invite.setCreatedAt(LocalDateTime.now());

        return mapCreated(repo.save(invite));
    }

    public void accept(String token, Long userId) {
        StoreInvite invite = repo
                .findByTokenAndUsedAtIsNullAndExpiresAtAfter(
                        token,
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.INVALID_INVITE,
                        "Convite inválido ou expirado."
                ));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.USER_NOT_FOUND,
                        "Usuário não encontrado."
                ));

        if (!user.getEmail().equalsIgnoreCase(invite.getEmail())) {
            throw new ForbiddenException(
                    ErrorCode.ACCESS_DENIED,
                    "Este convite pertence a outro email."
            );
        }

        boolean alreadyMember = storeUserRepository.existsByUserIdAndStoreId(
                user.getId(),
                invite.getStore().getId()
        );

        if (alreadyMember) {
            throw new ConflictException(
                    ErrorCode.INVALID_OPERATION,
                    "Usuário já pertence à loja."
            );
        }

        StoreUser su = new StoreUser();
        su.setStore(invite.getStore());
        su.setUser(user);
        su.setRole(Role.ADMIN);
        su.setCreatedAt(LocalDateTime.now());

        storeUserRepository.save(su);

        invite.setUsedAt(LocalDateTime.now());

        repo.save(invite);
    }

    public List<StoreInviteResponse> listByStore(String storeSlug, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        access.checkOwnerAccess(userId, store.getId());

        return repo.findByStoreIdAndUsedAtIsNull(store.getId())
                .stream()
                .map(this::map)
                .toList();
    }

    public void delete(String storeSlug, Long inviteId, Long userId) {
        Store store = getStoreBySlug(storeSlug);

        StoreInvite invite = repo.findById(inviteId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.INVITE_NOT_FOUND,
                        "Convite não encontrado."
                ));

        access.checkOwnerAccess(userId, store.getId());

        if (!invite.getStore().getId().equals(store.getId())) {
            throw new ForbiddenException(
                    ErrorCode.RESOURCE_NOT_IN_STORE,
                    "Convite não pertence à loja."
            );
        }

        repo.delete(invite);
    }

    public StoreInviteResponse validateToken(String token) {
        StoreInvite invite = repo
                .findByTokenAndUsedAtIsNullAndExpiresAtAfter(token, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.INVALID_INVITE,
                        "Convite inválido ou expirado."
                ));

        return map(invite);
    }

    private Store getStoreBySlug(String slug) {
        return storeRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.STORE_NOT_FOUND,
                        "Loja não encontrada."
                ));
    }

    private StoreInviteResponse map(StoreInvite i) {
        return StoreInviteResponse.builder()
                .id(i.getId())
                .email(i.getEmail())
                .expiresAt(i.getExpiresAt())
                .usedAt(i.getUsedAt())
                .build();
    }

    private StoreInviteCreateResponse mapCreated(StoreInvite i) {
        return StoreInviteCreateResponse.builder()
                .id(i.getId())
                .email(i.getEmail())
                .token(i.getToken())
                .expiresAt(i.getExpiresAt())
                .usedAt(i.getUsedAt())
                .build();
    }
}