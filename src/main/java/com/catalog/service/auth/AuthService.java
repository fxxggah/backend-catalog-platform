package com.catalog.service.auth;

import com.catalog.domain.entity.User;
import com.catalog.domain.enums.Provider;
import com.catalog.dto.auth.AuthResponse;
import com.catalog.dto.auth.GoogleUserData;
import com.catalog.dto.user.UserResponse;
import com.catalog.exception.BadRequestException;
import com.catalog.exception.ErrorCode;
import com.catalog.provider.GoogleTokenVerifier;
import com.catalog.repository.UserRepository;
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
        if (token == null || token.isBlank()) {
            throw new BadRequestException(
                    ErrorCode.VALIDATION_ERROR,
                    "Token do Google não enviado."
            );
        }

        GoogleUserData googleUser = googleTokenVerifier.verify(token);

        User user = userRepository.findByEmail(googleUser.getEmail())
                .orElseGet(() -> createUser(googleUser));

        String jwt = jwtService.generateToken(user.getId());

        return AuthResponse.builder()
                .token(jwt)
                .user(
                        UserResponse.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .provider(user.getProvider())
                                .active(user.getActive())
                                .createdAt(user.getCreatedAt())
                                .build()
                )
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