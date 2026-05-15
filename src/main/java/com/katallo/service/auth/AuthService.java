package com.katallo.service.auth;

import com.katallo.domain.entity.User;
import com.katallo.domain.enums.Provider;
import com.katallo.dto.auth.AuthResponse;
import com.katallo.dto.auth.GoogleUserData;
import com.katallo.dto.user.UserResponse;
import com.katallo.exception.BadRequestException;
import com.katallo.domain.enums.ErrorCode;
import com.katallo.provider.GoogleTokenVerifier;
import com.katallo.repository.UserRepository;
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