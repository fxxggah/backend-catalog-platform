package com.katallo.controller.viewer;

import com.katallo.annotation.CurrentUser;
import com.katallo.dto.storeinvite.StoreInviteResponse;
import com.katallo.service.StoreInviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invites")
@RequiredArgsConstructor
public class PublicStoreInviteController {

    private final StoreInviteService storeInviteService;

    @GetMapping("/validate/{token}")
    public ResponseEntity<StoreInviteResponse> validate(
            @PathVariable String token) {

        return ResponseEntity.ok(
                storeInviteService.validateToken(token)
        );
    }

    @PostMapping("/accept/{token}")
    public ResponseEntity<Void> accept(
            @PathVariable String token,
            @CurrentUser Long userId) {

        storeInviteService.accept(token, userId);

        return ResponseEntity.noContent().build();
    }
}