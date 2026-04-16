package com.catalog.controller.owner;

import com.catalog.dto.store.StoreRequest;
import com.catalog.dto.store.StoreResponse;
import com.catalog.service.StoreService;
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
            @RequestHeader("userId") Long userId) {

        return ResponseEntity.ok(
                storeService.update(storeSlug, request, userId)
        );
    }

    @DeleteMapping
    public ResponseEntity<Void> deactivate(
            @PathVariable String storeSlug,
            @RequestHeader("userId") Long userId) {

        storeService.deactivate(storeSlug, userId);
        return ResponseEntity.noContent().build();
    }
}