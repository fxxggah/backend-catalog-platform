package com.catalog.repository;

import com.catalog.domain.entity.Store;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Testes do StoreRepository")
class StoreRepositoryTest {

    @Autowired
    private StoreRepository storeRepository;

    @Test
    @DisplayName("Deve encontrar loja pelo slug")
    void deveEncontrarLojaPorSlug() {
        Store store = criarLoja("Loja Teste", "loja-teste", true);
        storeRepository.save(store);

        var result = storeRepository.findBySlug("loja-teste");

        assertThat(result).isPresent();
        assertThat(result.get().getSlug()).isEqualTo("loja-teste");
    }

    @Test
    @DisplayName("Deve encontrar loja ativa pelo slug")
    void deveEncontrarLojaAtivaPorSlug() {
        Store store = criarLoja("Loja Ativa", "loja-ativa", true);
        storeRepository.save(store);

        var result = storeRepository.findBySlugAndActiveTrue("loja-ativa");

        assertThat(result).isPresent();
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("Não deve encontrar loja inativa ao buscar por slug ativo")
    void naoDeveEncontrarLojaInativaPorSlugAtivo() {
        Store store = criarLoja("Loja Inativa", "loja-inativa", false);
        storeRepository.save(store);

        var result = storeRepository.findBySlugAndActiveTrue("loja-inativa");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve listar apenas lojas ativas")
    void deveListarApenasLojasAtivas() {
        storeRepository.save(criarLoja("Loja Ativa", "ativa", true));
        storeRepository.save(criarLoja("Loja Inativa", "inativa", false));

        var result = storeRepository.findByActiveTrue();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("ativa");
    }

    @Test
    @DisplayName("Deve verificar se existe loja pelo slug")
    void deveVerificarSeExisteLojaPorSlug() {
        storeRepository.save(criarLoja("Loja Teste", "loja-teste", true));

        boolean exists = storeRepository.existsBySlug("loja-teste");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve verificar se existe slug em outra loja")
    void deveVerificarSeExisteSlugEmOutraLoja() {
        Store store1 = storeRepository.save(criarLoja("Loja 1", "minha-loja", true));
        Store store2 = storeRepository.save(criarLoja("Loja 2", "outra-loja", true));

        boolean exists = storeRepository.existsBySlugAndIdNot("minha-loja", store2.getId());

        assertThat(exists).isTrue();
        assertThat(store1.getId()).isNotEqualTo(store2.getId());
    }

    private Store criarLoja(String name, String slug, boolean active) {
        return Store.builder()
                .name(name)
                .slug(slug)
                .active(active)
                .primaryColor("#111111")
                .secondaryColor("#ffffff")
                .tertiaryColor("#f5f5f5")
                .template("minimal")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}