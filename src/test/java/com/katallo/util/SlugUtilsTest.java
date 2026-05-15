package com.katallo.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do SlugUtils")
class SlugUtilsTest {

    @Test
    @DisplayName("Deve gerar slug removendo acentos e espaços")
    void deveGerarSlugRemovendoAcentosEEspacos() {
        String slug = SlugUtils.toSlug("Loja da Tia Áéíóú");

        assertThat(slug).isEqualTo("loja-da-tia-aeiou");
    }

    @Test
    @DisplayName("Deve substituir múltiplos espaços e underscores por hífen")
    void deveSubstituirMultiplosEspacosEUnderscoresPorHifen() {
        String slug = SlugUtils.toSlug("Minha___Loja   Premium");

        assertThat(slug).isEqualTo("minha-loja-premium");
    }

    @Test
    @DisplayName("Deve remover caracteres especiais")
    void deveRemoverCaracteresEspeciais() {
        String slug = SlugUtils.toSlug("Vestido Floral! @ Azul #01");

        assertThat(slug).isEqualTo("vestido-floral-azul-01");
    }

    @Test
    @DisplayName("Deve lançar exceção quando texto for nulo")
    void deveLancarExcecaoQuandoTextoForNulo() {
        assertThatThrownBy(() -> SlugUtils.toSlug(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome inválido para gerar slug");
    }

    @Test
    @DisplayName("Deve lançar exceção quando texto estiver em branco")
    void deveLancarExcecaoQuandoTextoEstiverEmBranco() {
        assertThatThrownBy(() -> SlugUtils.toSlug("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome inválido para gerar slug");
    }
}
