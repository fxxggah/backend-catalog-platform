package com.katallo.service.auth;

import com.katallo.exception.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do AuthContextService")
class AuthContextServiceTest {

    private final AuthContextService authContextService = new AuthContextService();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve retornar id do usuário quando principal for Long")
    void deveRetornarIdDoUsuarioQuandoPrincipalForLong() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(10L, null)
        );

        Long userId = authContextService.getUserId();

        assertThat(userId).isEqualTo(10L);
    }

    @Test
    @DisplayName("Deve retornar id do usuário quando principal for String")
    void deveRetornarIdDoUsuarioQuandoPrincipalForString() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("15", null)
        );

        Long userId = authContextService.getUserId();

        assertThat(userId).isEqualTo(15L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando não existir autenticação")
    void deveLancarExcecaoQuandoNaoExistirAutenticacao() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(authContextService::getUserId)
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Usuário não autenticado.");
    }

    @Test
    @DisplayName("Deve lançar exceção quando principal for inválido")
    void deveLancarExcecaoQuandoPrincipalForInvalido() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new Object(), null)
        );

        assertThatThrownBy(authContextService::getUserId)
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Usuário autenticado inválido.");
    }
}
