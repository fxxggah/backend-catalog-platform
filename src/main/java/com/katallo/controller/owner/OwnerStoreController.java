package com.katallo.controller.owner;

import com.katallo.annotation.CurrentUser;
import com.katallo.dto.store.StoreRequest;
import com.katallo.dto.store.StoreResponse;
import com.katallo.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/stores/{storeSlug}")
@RequiredArgsConstructor
public class OwnerStoreController {

    private final StoreService storeService;

    @PutMapping
    public ResponseEntity<StoreResponse> update(
            @PathVariable String storeSlug,
            @Valid @RequestBody StoreRequest request,
            @CurrentUser Long userId) {

        return ResponseEntity.ok(
                storeService.update(storeSlug, request, userId)
        );
    }

    @DeleteMapping
    public ResponseEntity<Void> deactivate(
            @PathVariable String storeSlug,
            @CurrentUser Long userId) {

        storeService.deactivate(storeSlug, userId);
        return ResponseEntity.noContent().build();
    }
}