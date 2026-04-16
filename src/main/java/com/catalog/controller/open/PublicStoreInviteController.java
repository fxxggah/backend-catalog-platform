package com.catalog.controller.open;

import com.catalog.domain.entity.User;
import com.catalog.dto.storeinvite.StoreInviteResponse;
import com.catalog.service.StoreInviteService;
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
            @RequestHeader("userId") Long userId) {

        User user = new User();
        user.setId(userId);

        storeInviteService.accept(token, user);

        return ResponseEntity.noContent().build();
    }
}