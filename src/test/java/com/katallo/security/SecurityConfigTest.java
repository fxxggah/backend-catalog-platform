package com.katallo.security;

import com.katallo.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@DisplayName("Testes do SecurityConfig")
class SecurityConfigTest {

    @Test
    @DisplayName("Deve rejeitar autenticação por usuário e senha")
    void deveRejeitarAutenticacaoPorUsuarioESenha() {
        SecurityConfig config = new SecurityConfig(mock(JwtAuthenticationFilter.class));

        var userDetailsService = config.userDetailsService();

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("usuario@email.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Autenticação por usuário/senha não é suportada.");
    }
}
