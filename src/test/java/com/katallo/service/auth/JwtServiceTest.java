package com.katallo.service.auth;

import com.katallo.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do JwtService")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "12345678901234567890123456789012");
        ReflectionTestUtils.setField(jwtService, "expiration", 86_400_000L);
    }

    @Test
    @DisplayName("Deve gerar token válido e extrair id do usuário")
    void deveGerarTokenValidoEExtrairIdDoUsuario() {
        String token = jwtService.generateToken(10L);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.getUserId(token)).isEqualTo(10L);
    }

    @Test
    @DisplayName("Deve lançar exceção para token inválido")
    void deveLancarExcecaoParaTokenInvalido() {
        assertThatThrownBy(() -> jwtService.isValid("token-invalido"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Token inválido.");
    }

    @Test
    @DisplayName("Deve lançar exceção quando token não for enviado")
    void deveLancarExcecaoQuandoTokenNaoForEnviado() {
        assertThatThrownBy(() -> jwtService.getUserId(" "))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Token não enviado.");
    }
}
