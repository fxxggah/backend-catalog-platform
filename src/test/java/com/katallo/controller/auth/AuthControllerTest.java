package com.katallo.controller.auth;

import com.katallo.dto.auth.AuthResponse;
import com.katallo.dto.auth.GoogleLoginRequest;
import com.katallo.dto.user.UserResponse;
import com.katallo.service.auth.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AuthController")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("Deve realizar login com Google")
    void deveRealizarLoginComGoogle() {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setToken("google-token");

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-token")
                .user(UserResponse.builder().id(1L).name("Gabriel").email("gabriel@email.com").build())
                .build();

        when(authService.loginWithGoogle("google-token")).thenReturn(authResponse);

        var response = authController.loginWithGoogle(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(authResponse);
        verify(authService).loginWithGoogle("google-token");
    }
}
