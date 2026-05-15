package com.katallo.controller.owner;

import com.katallo.annotation.CurrentUser;
import com.katallo.dto.storeinvite.StoreInviteCreateResponse;
import com.katallo.dto.storeinvite.StoreInviteRequest;
import com.katallo.dto.storeinvite.StoreInviteResponse;
import com.katallo.service.StoreInviteService;
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
    public ResponseEntity<StoreInviteCreateResponse> invite(
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