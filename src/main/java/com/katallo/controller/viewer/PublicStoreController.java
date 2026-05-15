package com.katallo.controller.viewer;

import com.katallo.dto.store.StoreResponse;
import com.katallo.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class PublicStoreController {

    private final StoreService storeService;

    @GetMapping("/{slug}")
    public ResponseEntity<StoreResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(storeService.getBySlug(slug));
    }
}