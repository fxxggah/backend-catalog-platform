package com.katallo.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do CorsConfig")
class CorsConfigTest {

    @Test
    @DisplayName("Deve configurar CORS para o frontend local")
    void deveConfigurarCorsParaOFrontendLocal() {
        CorsConfig corsConfig = new CorsConfig();

        var source = corsConfig.corsConfigurationSource();
        var request = new MockHttpServletRequest("GET", "/api/v1/stores/minha-loja");
        var config = source.getCorsConfiguration(request);

        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).contains("http://localhost:3000");
        assertThat(config.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(config.getAllowedHeaders()).contains("*");
        assertThat(config.getAllowCredentials()).isTrue();
    }
}
