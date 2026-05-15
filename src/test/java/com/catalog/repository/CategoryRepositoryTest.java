package com.catalog.repository;

import com.catalog.domain.entity.Category;
import com.catalog.domain.entity.Store;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
        Store store = storeRepository.save(criarLoja("loja-1"));
        Store otherStore = storeRepository.save(criarLoja("loja-2"));

        categoryRepository.save(criarCategoria("Vestidos", "vestidos", store, null));
        categoryRepository.save(criarCategoria("Calçados", "calcados", otherStore, null));

        var result = categoryRepository.findByStoreId(store.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("vestidos");
    }

    @Test
    @DisplayName("Deve encontrar categoria por loja e slug")
    void deveEncontrarCategoriaPorLojaESlug() {
        Store store = storeRepository.save(criarLoja("loja-1"));
        categoryRepository.save(criarCategoria("Vestidos", "vestidos", store, null));

        var result = categoryRepository.findByStoreIdAndSlug(store.getId(), "vestidos");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Vestidos");
    }

    @Test
    @DisplayName("Deve listar apenas categorias não deletadas")
    void deveListarApenasCategoriasNaoDeletadas() {
        Store store = storeRepository.save(criarLoja("loja-1"));

        categoryRepository.save(criarCategoria("Ativa", "ativa", store, null));
        categoryRepository.save(criarCategoria("Deletada", "deletada", store, LocalDateTime.now()));

        var result = categoryRepository.findByStoreIdAndDeletedAtIsNull(store.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("ativa");
    }

    @Test
    @DisplayName("Deve encontrar categoria não deletada por loja e slug")
    void deveEncontrarCategoriaNaoDeletadaPorLojaESlug() {
        Store store = storeRepository.save(criarLoja("loja-1"));
        categoryRepository.save(criarCategoria("Ativa", "ativa", store, null));

        var result = categoryRepository.findByStoreIdAndSlugAndDeletedAtIsNull(store.getId(), "ativa");

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Não deve encontrar categoria deletada por loja e slug quando buscar apenas ativas")
    void naoDeveEncontrarCategoriaDeletadaPorLojaESlug() {
        Store store = storeRepository.save(criarLoja("loja-1"));
        categoryRepository.save(criarCategoria("Deletada", "deletada", store, LocalDateTime.now()));

        var result = categoryRepository.findByStoreIdAndSlugAndDeletedAtIsNull(store.getId(), "deletada");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar se existe categoria por loja e slug")
    void deveVerificarSeExisteCategoriaPorLojaESlug() {
        Store store = storeRepository.save(criarLoja("loja-1"));
        categoryRepository.save(criarCategoria("Vestidos", "vestidos", store, null));

        boolean exists = categoryRepository.existsByStoreIdAndSlug(store.getId(), "vestidos");

        assertThat(exists).isTrue();
    }

    private Store criarLoja(String slug) {
        return Store.builder()
                .name("Loja " + slug)
                .slug(slug)
                .active(true)
                .template("minimal")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Category criarCategoria(String name, String slug, Store store, LocalDateTime deletedAt) {
        return Category.builder()
                .name(name)
                .slug(slug)
                .store(store)
                .deletedAt(deletedAt)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}