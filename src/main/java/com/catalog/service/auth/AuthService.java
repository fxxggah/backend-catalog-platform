package com.catalog.service.auth;

import com.catalog.domain.entity.User;
import com.catalog.domain.enums.Provider;
import com.catalog.dto.auth.AuthResponse;
import com.catalog.dto.auth.GoogleUserData;
import com.catalog.repository.UserRepository;
import com.catalog.provider.GoogleTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthResponse loginWithGoogle(String token) {

        // 🔥 1. valida token do google
        GoogleUserData googleUser = googleTokenVerifier.verify(token);

        // 🔥 2. busca usuário por email
        User user = userRepository.findByEmail(googleUser.getEmail())
                .orElseGet(() -> createUser(googleUser));

        // 🔥 3. gera JWT
        String jwt = jwtService.generateToken(user.getId());

        return AuthResponse.builder()
                .token(jwt)
                .build();
    }

    private User createUser(GoogleUserData googleUser) {

        User user = new User();
        user.setName(googleUser.getName());
        user.setEmail(googleUser.getEmail());
        user.setProvider(Provider.GOOGLE);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}