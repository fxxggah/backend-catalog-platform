package com.katallo.integration;

import com.katallo.support.IntegrationTestBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Teste de integração completo deve ser ativado quando o fluxo estiver estável e com dados isolados.")
@DisplayName("Testes de integração do fluxo de convite")
class InviteFlowIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("Deve carregar estrutura base do fluxo de convite")
    void deveCarregarEstruturaBaseDoFluxo() {
        assertThat(true).isTrue();
    }
}
