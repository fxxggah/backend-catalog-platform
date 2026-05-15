package com.katallo.repository;

import com.katallo.domain.entity.Store;
import com.katallo.domain.enums.StoreTemplate;
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
        assertThat(result.get().getActive()).isTrue();
    }

    @Test
    @DisplayName("Não deve encontrar loja inativa ao buscar por slug ativo")
    void naoDeveEncontrarLojaInativaAoBuscarPorSlugAtivo() {
        Store store = criarLoja("Loja Inativa", "loja-inativa", false);
        storeRepository.save(store);

        var result = storeRepository.findBySlugAndActiveTrue("loja-inativa");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve listar apenas lojas ativas")
    void deveListarApenasLojasAtivas() {
        storeRepository.save(criarLoja("Loja Ativa", "loja-ativa-lista", true));
        storeRepository.save(criarLoja("Loja Inativa", "loja-inativa-lista", false));

        var result = storeRepository.findByActiveTrue();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("loja-ativa-lista");
    }

    @Test
    @DisplayName("Deve verificar se existe loja pelo slug")
    void deveVerificarSeExisteLojaPorSlug() {
        storeRepository.save(criarLoja("Loja Teste", "loja-existe", true));

        boolean exists = storeRepository.existsBySlug("loja-existe");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve verificar se existe slug em outra loja")
    void deveVerificarSeExisteSlugEmOutraLoja() {
        Store store1 = storeRepository.save(criarLoja("Loja 1", "slug-repetido", true));
        Store store2 = storeRepository.save(criarLoja("Loja 2", "slug-diferente", true));

        boolean exists = storeRepository.existsBySlugAndIdNot("slug-repetido", store2.getId());

        assertThat(exists).isTrue();
        assertThat(store1.getId()).isNotEqualTo(store2.getId());
    }

    private Store criarLoja(String name, String slug, boolean active) {
        Store store = new Store();
        store.setName(name);
        store.setSlug(slug);
        store.setTemplate(StoreTemplate.MINIMAL);
        store.setActive(active);
        store.setCreatedAt(LocalDateTime.now());
        store.setUpdatedAt(LocalDateTime.now());
        return store;
    }
}