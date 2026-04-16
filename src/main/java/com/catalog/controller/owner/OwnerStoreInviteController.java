package com.catalog.controller.owner;

import com.catalog.annotation.CurrentUser;
import com.catalog.dto.storeinvite.StoreInviteRequest;
import com.catalog.dto.storeinvite.StoreInviteResponse;
import com.catalog.service.StoreInviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/stores/{storeSlug}/invites")
@RequiredArgsConstructor
@Validated
public class OwnerStoreInviteController {

    private final StoreInviteService storeInviteService;

    @PostMapping
    public ResponseEntity<StoreInviteResponse> invite(
            @PathVariable String storeSlug,
            @Valid @RequestBody StoreInviteRequest request,
            @CurrentUser Long userId) {

        return ResponseEntity.status(201)
                .body(storeInviteService.invite(storeSlug, request, userId));
    }

    @GetMapping
    public ResponseEntity<List<StoreInviteResponse>> list(
            @PathVariable String storeSlug,
            @CurrentUser Long userId) {

        return ResponseEntity.ok(
                storeInviteService.listByStore(storeSlug, userId)
        );
    }

    @DeleteMapping("/{inviteId}")
    public ResponseEntity<Void> delete(
            @PathVariable String storeSlug,
            @PathVariable Long inviteId,
            @CurrentUser Long userId) {

        storeInviteService.delete(storeSlug, inviteId, userId);
        return ResponseEntity.noContent().build();
    }
}