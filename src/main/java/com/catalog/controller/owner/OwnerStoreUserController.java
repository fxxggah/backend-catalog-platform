package com.catalog.controller.owner;

import com.catalog.service.auth.AuthContextService;
import com.catalog.dto.storeuser.StoreUserResponse;
import com.catalog.service.StoreUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/stores/{storeSlug}/users")
@RequiredArgsConstructor
@Validated
public class OwnerStoreUserController {

    private final StoreUserService storeUserService;
    private final AuthContextService authContextService;

    @GetMapping
    public ResponseEntity<List<StoreUserResponse>> list(
            @PathVariable String storeSlug,
            @RequestHeader("userId") Long userId) {

        return ResponseEntity.ok(
                storeUserService.listByStore(storeSlug, userId)
        );
    }

    @DeleteMapping("/{userIdToRemove}")
    public ResponseEntity<Void> removeUser(
            @PathVariable String storeSlug,
            @PathVariable Long userIdToRemove,
            @RequestHeader("userId") Long userId) {

        storeUserService.removeUser(storeSlug, userIdToRemove, userId);
        return ResponseEntity.noContent().build();
    }
}