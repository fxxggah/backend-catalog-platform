package com.katallo.repository;

import com.katallo.domain.entity.Category;
import com.katallo.domain.entity.Product;
import com.katallo.domain.entity.Store;
import com.katallo.domain.enums.StoreTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Testes do ProductRepository")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Deve listar produtos públicos da loja ordenando em estoque primeiro e mais recentes depois")
    void deveListarProdutosPublicosDaLojaOrdenandoEmEstoquePrimeiroEMaisRecentesDepois() {
        Store store = storeRepository.save(criarLoja("loja-produtos-publicos"));
        Category category = categoryRepository.save(criarCategoria("Roupas", "roupas", store));

        Product produtoSemEstoque = criarProduto("Produto sem estoque", "produto-sem-estoque", store, category, false, false, null, LocalDateTime.now().plusDays(3));
        Product produtoAntigo = criarProduto("Produto antigo", "produto-antigo", store, category, true, false, null, LocalDateTime.now().minusDays(2));
        Product produtoNovo = criarProduto("Produto novo", "produto-novo", store, category, true, false, null, LocalDateTime.now().plusDays(1));
        Product produtoDeletado = criarProduto("Produto deletado", "produto-deletado", store, category, true, false, LocalDateTime.now(), LocalDateTime.now().plusDays(5));

        productRepository.save(produtoSemEstoque);
        productRepository.save(produtoAntigo);
        productRepository.save(produtoNovo);
        productRepository.save(produtoDeletado);

        var result = productRepository.findPublicByStoreIdOrderByInStockFirst(
                store.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getSlug()).isEqualTo("produto-novo");
        assertThat(result.getContent().get(1).getSlug()).isEqualTo("produto-antigo");
        assertThat(result.getContent().get(2).getSlug()).isEqualTo("produto-sem-estoque");
    }

    @Test
    @DisplayName("Deve buscar produtos públicos da loja pelo nome ignorando maiúsculas e minúsculas")
    void deveBuscarProdutosPublicosDaLojaPorNomeIgnorandoMaiusculasEMinusculas() {
        Store store = storeRepository.save(criarLoja("loja-busca-produto"));
        Category category = categoryRepository.save(criarCategoria("Roupas", "roupas", store));

        productRepository.save(criarProduto("Vestido Floral", "vestido-floral", store, category, true, false, null, LocalDateTime.now()));
        productRepository.save(criarProduto("Camisa Básica", "camisa-basica", store, category, true, false, null, LocalDateTime.now()));

        var result = productRepository.findPublicByStoreIdAndNameOrderByInStockFirst(
                store.getId(),
                "vestido",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Vestido Floral");
    }

    @Test
    @DisplayName("Deve listar produtos públicos por loja e categoria")
    void deveListarProdutosPublicosPorLojaECategoria() {
        Store store = storeRepository.save(criarLoja("loja-produtos-categoria"));
        Category roupas = categoryRepository.save(criarCategoria("Roupas", "roupas", store));
        Category calcados = categoryRepository.save(criarCategoria("Calçados", "calcados", store));

        productRepository.save(criarProduto("Vestido", "vestido", store, roupas, true, false, null, LocalDateTime.now()));
        productRepository.save(criarProduto("Tênis", "tenis", store, calcados, true, false, null, LocalDateTime.now()));

        var result = productRepository.findPublicByStoreIdAndCategoryIdOrderByInStockFirst(
                store.getId(),
                roupas.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSlug()).isEqualTo("vestido");
    }

    @Test
    @DisplayName("Deve listar produtos não deletados por loja")
    void deveListarProdutosNaoDeletadosPorLoja() {
        Store store = storeRepository.save(criarLoja("loja-produtos-ativos"));
        Category category = categoryRepository.save(criarCategoria("Roupas", "roupas", store));

        productRepository.save(criarProduto("Ativo", "ativo", store, category, true, false, null, LocalDateTime.now()));
        productRepository.save(criarProduto("Deletado", "deletado", store, category, true, false, LocalDateTime.now(), LocalDateTime.now()));

        var result = productRepository.findByStoreIdAndDeletedAtIsNull(
                store.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSlug()).isEqualTo("ativo");
    }

    @Test
    @DisplayName("Deve buscar produtos não deletados por loja e nome")
    void deveBuscarProdutosNaoDeletadosPorLojaENome() {
        Store store = storeRepository.save(criarLoja("loja-produtos-nome"));
        Category category = categoryRepository.save(criarCategoria("Roupas", "roupas", store));

        productRepository.save(criarProduto("Vestido Azul", "vestido-azul", store, category, true, false, null, LocalDateTime.now()));
        productRepository.save(criarProduto("Vestido Deletado", "vestido-deletado", store, category, true, false, LocalDateTime.now(), LocalDateTime.now()));

        var result = productRepository.findByStoreIdAndNameContainingIgnoreCaseAndDeletedAtIsNull(
                store.getId(),
                "vestido",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSlug()).isEqualTo("vestido-azul");
    }

    @Test
    @DisplayName("Deve encontrar produto por loja e slug")
    void deveEncontrarProdutoPorLojaESlug() {
        Store store = storeRepository.save(criarLoja("loja-produto-slug"));
        Category category = categoryRepository.save(criarCategoria("Roupas", "roupas", store));
        productRepository.save(criarProduto("Vestido", "vestido", store, category, true, false, null, LocalDateTime.now()));

        var result = productRepository.findByStoreIdAndSlug(store.getId(), "vestido");

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Deve encontrar produto não deletado por loja e slug")
    void deveEncontrarProdutoNaoDeletadoPorLojaESlug() {
        Store store = storeRepository.save(criarLoja("loja-produto-ativo-slug"));
        Category category = categoryRepository.save(criarCategoria("Roupas", "roupas", store));
        productRepository.save(criarProduto("Vestido", "vestido", store, category, true, false, null, LocalDateTime.now()));

        var result = productRepository.findByStoreIdAndSlugAndDeletedAtIsNull(store.getId(), "vestido");

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Não deve encontrar produto deletado ao buscar por loja, slug e não deletado")
    void naoDeveEncontrarProdutoDeletadoAoBuscarPorLojaSlugENaoDeletado() {
        Store store = storeRepository.save(criarLoja("loja-produto-deletado-slug"));
        Category category = categoryRepository.save(criarCategoria("Roupas", "roupas", store));
        productRepository.save(criarProduto("Vestido", "vestido", store, category, true, false, LocalDateTime.now(), LocalDateTime.now()));

        var result = productRepository.findByStoreIdAndSlugAndDeletedAtIsNull(store.getId(), "vestido");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar se existe produto por loja e slug")
    void deveVerificarSeExisteProdutoPorLojaESlug() {
        Store store = storeRepository.save(criarLoja("loja-existe-produto"));
        Category category = categoryRepository.save(criarCategoria("Roupas", "roupas", store));
        productRepository.save(criarProduto("Vestido", "vestido", store, category, true, false, null, LocalDateTime.now()));

        boolean exists = productRepository.existsByStoreIdAndSlug(store.getId(), "vestido");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve listar até oito produtos destacados em estoque e não deletados")
    void deveListarAteOitoProdutosDestacadosEmEstoqueENaoDeletados() {
        Store store = storeRepository.save(criarLoja("loja-destaques"));
        Category category = categoryRepository.save(criarCategoria("Roupas", "roupas", store));

        productRepository.save(criarProduto("Destacado", "destacado", store, category, true, true, null, LocalDateTime.now()));
        productRepository.save(criarProduto("Não destacado", "nao-destacado", store, category, true, false, null, LocalDateTime.now()));
        productRepository.save(criarProduto("Sem estoque", "sem-estoque", store, category, false, true, null, LocalDateTime.now()));
        productRepository.save(criarProduto("Deletado", "deletado", store, category, true, true, LocalDateTime.now(), LocalDateTime.now()));

        var result = productRepository.findTop8ByStoreIdAndFeaturedTrueAndInStockTrueAndDeletedAtIsNullOrderByCreatedAtDesc(store.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("destacado");
    }

    @Test
    @DisplayName("Deve listar até oito produtos em estoque e não deletados")
    void deveListarAteOitoProdutosEmEstoqueENaoDeletados() {
        Store store = storeRepository.save(criarLoja("loja-em-estoque"));
        Category category = categoryRepository.save(criarCategoria("Roupas", "roupas", store));

        productRepository.save(criarProduto("Em estoque", "em-estoque", store, category, true, false, null, LocalDateTime.now()));
        productRepository.save(criarProduto("Sem estoque", "sem-estoque", store, category, false, false, null, LocalDateTime.now()));
        productRepository.save(criarProduto("Deletado", "deletado", store, category, true, false, LocalDateTime.now(), LocalDateTime.now()));

        var result = productRepository.findTop8ByStoreIdAndInStockTrueAndDeletedAtIsNullOrderByCreatedAtDesc(store.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("em-estoque");
    }

    @Test
    @DisplayName("Deve listar produtos relacionados excluindo o próprio produto")
    void deveListarProdutosRelacionadosExcluindoOProprioProduto() {
        Store store = storeRepository.save(criarLoja("loja-relacionados"));
        Category category = categoryRepository.save(criarCategoria("Roupas", "roupas", store));

        Product produtoAtual = productRepository.save(criarProduto("Produto Atual", "produto-atual", store, category, true, false, null, LocalDateTime.now()));
        productRepository.save(criarProduto("Relacionado", "relacionado", store, category, true, false, null, LocalDateTime.now()));
        productRepository.save(criarProduto("Sem Estoque", "sem-estoque", store, category, false, false, null, LocalDateTime.now()));

        var result = productRepository.findByStoreIdAndCategoryIdAndIdNotAndInStockTrueAndDeletedAtIsNull(
                store.getId(),
                category.getId(),
                produtoAtual.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("relacionado");
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

    private Category criarCategoria(String name, String slug, Store store) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setStore(store);
        category.setCreatedAt(LocalDateTime.now());
        return category;
    }

    private Product criarProduto(
            String name,
            String slug,
            Store store,
            Category category,
            boolean inStock,
            boolean featured,
            LocalDateTime deletedAt,
            LocalDateTime createdAt
    ) {
        Product product = new Product();
        product.setName(name);
        product.setSlug(slug);
        product.setDescription("Descrição do produto");
        product.setPrice(BigDecimal.valueOf(100.00));
        product.setPromotionalPrice(BigDecimal.valueOf(89.90));
        product.setStore(store);
        product.setCategory(category);
        product.setInStock(inStock);
        product.setFeatured(featured);
        product.setDeletedAt(deletedAt);
        product.setCreatedAt(createdAt);
        product.setUpdatedAt(createdAt);
        return product;
    }
}