package com.katallo.repository;

import com.katallo.domain.entity.Category;
import com.katallo.domain.entity.Store;
import com.katallo.domain.enums.StoreTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Testes do CategoryRepository")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    @DisplayName("Deve listar categorias por loja")
    void deveListarCategoriasPorLoja() {
        Store store = storeRepository.save(criarLoja("loja-categorias"));
        Store outraStore = storeRepository.save(criarLoja("outra-loja-categorias"));

        categoryRepository.save(criarCategoria("Vestidos", "vestidos", store, null));
        categoryRepository.save(criarCategoria("Calçados", "calcados", outraStore, null));

        var result = categoryRepository.findByStoreId(store.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("vestidos");
    }

    @Test
    @DisplayName("Deve encontrar categoria por loja e slug")
    void deveEncontrarCategoriaPorLojaESlug() {
        Store store = storeRepository.save(criarLoja("loja-categoria-slug"));
        categoryRepository.save(criarCategoria("Vestidos", "vestidos", store, null));

        var result = categoryRepository.findByStoreIdAndSlug(store.getId(), "vestidos");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Vestidos");
    }

    @Test
    @DisplayName("Deve listar apenas categorias não deletadas")
    void deveListarApenasCategoriasNaoDeletadas() {
        Store store = storeRepository.save(criarLoja("loja-categorias-ativas"));

        categoryRepository.save(criarCategoria("Ativa", "ativa", store, null));
        categoryRepository.save(criarCategoria("Deletada", "deletada", store, LocalDateTime.now()));

        var result = categoryRepository.findByStoreIdAndDeletedAtIsNull(store.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("ativa");
    }

    @Test
    @DisplayName("Deve encontrar categoria não deletada por loja e slug")
    void deveEncontrarCategoriaNaoDeletadaPorLojaESlug() {
        Store store = storeRepository.save(criarLoja("loja-categoria-nao-deletada"));
        categoryRepository.save(criarCategoria("Ativa", "ativa", store, null));

        var result = categoryRepository.findByStoreIdAndSlugAndDeletedAtIsNull(store.getId(), "ativa");

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Não deve encontrar categoria deletada ao buscar apenas não deletadas")
    void naoDeveEncontrarCategoriaDeletadaAoBuscarApenasNaoDeletadas() {
        Store store = storeRepository.save(criarLoja("loja-categoria-deletada"));
        categoryRepository.save(criarCategoria("Deletada", "deletada", store, LocalDateTime.now()));

        var result = categoryRepository.findByStoreIdAndSlugAndDeletedAtIsNull(store.getId(), "deletada");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar se existe categoria por loja e slug")
    void deveVerificarSeExisteCategoriaPorLojaESlug() {
        Store store = storeRepository.save(criarLoja("loja-exists-categoria"));
        categoryRepository.save(criarCategoria("Vestidos", "vestidos", store, null));

        boolean exists = categoryRepository.existsByStoreIdAndSlug(store.getId(), "vestidos");

        assertThat(exists).isTrue();
    }

    private Store criarLoja(String slug) {
        Store store = new Store();
        store.setName("Loja " + slug);
        store.setSlug(slug);
        store.setTemplate(StoreTemplate.MINIMAL);
        store.setActive(true);
        store.setCreatedAt(LocalDateTime.now());
        return store;
    }

    private Category criarCategoria(String name, String slug, Store store, LocalDateTime deletedAt) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setStore(store);
        category.setDeletedAt(deletedAt);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }
}