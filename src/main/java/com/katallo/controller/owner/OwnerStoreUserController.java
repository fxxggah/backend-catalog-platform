package com.katallo.controller.owner;

import com.katallo.annotation.CurrentUser;
import com.katallo.dto.storeuser.StoreUserResponse;
import com.katallo.service.StoreUserService;
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

    @GetMapping("/me")
    public ResponseEntity<StoreUserResponse> me(
            @PathVariable String storeSlug,
            @CurrentUser Long userId) {

        return ResponseEntity.ok(
                storeUserService.getCurrentUserInStore(storeSlug, userId)
        );
    }

    @GetMapping
    public ResponseEntity<List<StoreUserResponse>> list(
            @PathVariable String storeSlug,
            @CurrentUser Long userId) {

        return ResponseEntity.ok(
                storeUserService.listByStore(storeSlug, userId)
        );
    }

    @DeleteMapping("/{userIdToRemove}")
    public ResponseEntity<Void> removeUser(
            @PathVariable String storeSlug,
            @PathVariable Long userIdToRemove,
            @CurrentUser Long userId) {

        storeUserService.removeUser(storeSlug, userIdToRemove, userId);
        return ResponseEntity.noContent().build();
    }
}