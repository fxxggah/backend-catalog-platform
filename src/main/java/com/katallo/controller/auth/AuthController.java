package com.katallo.controller.auth;

import com.katallo.service.auth.AuthService;
import com.katallo.dto.auth.AuthResponse;
import com.katallo.dto.auth.GoogleLoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request) {

        return ResponseEntity.ok(
                authService.loginWithGoogle(request.getToken())
        );
    }
}