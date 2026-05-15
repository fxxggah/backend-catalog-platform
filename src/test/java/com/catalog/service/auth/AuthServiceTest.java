package com.catalog.service.auth;

import com.catalog.domain.entity.User;
import com.catalog.domain.enums.Provider;
import com.catalog.dto.auth.GoogleUserData;
import com.catalog.exception.BadRequestException;
import com.catalog.provider.GoogleTokenVerifier;
import com.catalog.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AuthService")
class AuthServiceTest {

    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Deve autenticar usuário existente com Google")
    void deveAutenticarUsuarioExistenteComGoogle() {
        User user = criarUsuario(1L, "Gabriel", "gabriel@email.com");

        when(googleTokenVerifier.verify("google-token"))
                .thenReturn(new GoogleUserData("google-id", "gabriel@email.com", "Gabriel"));
        when(userRepository.findByEmail("gabriel@email.com"))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(1L)).thenReturn("jwt-token");

        var response = authService.loginWithGoogle("google-token");

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getId()).isEqualTo(1L);
        assertThat(response.getUser().getEmail()).isEqualTo("gabriel@email.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve criar usuário quando login Google for de novo e-mail")
    void deveCriarUsuarioQuandoLoginGoogleForDeNovoEmail() {
        when(googleTokenVerifier.verify("google-token"))
                .thenReturn(new GoogleUserData("google-id", "novo@email.com", "Novo Usuário"));
        when(userRepository.findByEmail("novo@email.com"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });
        when(jwtService.generateToken(2L)).thenReturn("jwt-novo");

        var response = authService.loginWithGoogle("google-token");

        assertThat(response.getToken()).isEqualTo("jwt-novo");
        assertThat(response.getUser().getId()).isEqualTo(2L);
        assertThat(response.getUser().getName()).isEqualTo("Novo Usuário");
        assertThat(response.getUser().getProvider()).isEqualTo(Provider.GOOGLE);
        assertThat(response.getUser().getActive()).isTrue();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando token do Google não for enviado")
    void deveLancarExcecaoQuandoTokenDoGoogleNaoForEnviado() {
        assertThatThrownBy(() -> authService.loginWithGoogle(" "))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Token do Google não enviado.");

        verify(googleTokenVerifier, never()).verify(any());
    }

    private User criarUsuario(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setProvider(Provider.GOOGLE);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
