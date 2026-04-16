package com.catalog.controller.admin;

import com.catalog.auth.AuthContextService;
import com.catalog.dto.store.StoreRequest;
import com.catalog.dto.store.StoreResponse;
import com.catalog.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Validated
public class AdminStoreController {

    private final StoreService storeService;
    private final AuthContextService authContextService;

    @PostMapping
    public ResponseEntity<StoreResponse> create(
            @Valid @RequestBody StoreRequest request,
            @RequestHeader("userId") Long userId) {

        return ResponseEntity.status(201)
                .body(storeService.create(request, userId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<StoreResponse>> getUserStores(
            @RequestHeader("userId") Long userId) {

        return ResponseEntity.ok(
                storeService.getUserStores(userId)
        );
    }
}