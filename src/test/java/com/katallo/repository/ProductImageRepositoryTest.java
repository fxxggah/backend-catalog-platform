package com.katallo.repository;

import com.katallo.domain.entity.Category;
import com.katallo.domain.entity.Product;
import com.katallo.domain.entity.ProductImage;
import com.katallo.domain.entity.Store;
import com.katallo.domain.enums.StoreTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Testes do ProductImageRepository")
class ProductImageRepositoryTest {

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    @DisplayName("Deve listar imagens do produto ordenadas pela posição crescente")
    void deveListarImagensDoProdutoOrdenadasPelaPosicaoCrescente() {
        Store store = storeRepository.save(criarLoja("loja-imagens"));
        Category category = categoryRepository.save(criarCategoria(store));
        Product product = productRepository.save(criarProduto(store, category, "produto-imagens"));

        productImageRepository.save(criarImagem(product, "imagem-3.jpg", 3));
        productImageRepository.save(criarImagem(product, "imagem-1.jpg", 1));
        productImageRepository.save(criarImagem(product, "imagem-2.jpg", 2));

        var result = productImageRepository.findByProductIdOrderByPositionAsc(product.getId());

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getPosition()).isEqualTo(1);
        assertThat(result.get(1).getPosition()).isEqualTo(2);
        assertThat(result.get(2).getPosition()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve deletar imagens pelo id do produto")
    void deveDeletarImagensPeloIdDoProduto() {
        Store store = storeRepository.save(criarLoja("loja-deletar-imagens"));
        Category category = categoryRepository.save(criarCategoria(store));
        Product product = productRepository.save(criarProduto(store, category, "produto-deletar-imagens"));

        productImageRepository.save(criarImagem(product, "imagem-1.jpg", 1));
        productImageRepository.save(criarImagem(product, "imagem-2.jpg", 2));

        productImageRepository.deleteByProductId(product.getId());

        var result = productImageRepository.findByProductIdOrderByPositionAsc(product.getId());

        assertThat(result).isEmpty();
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

    private Category criarCategoria(Store store) {
        Category category = new Category();
        category.setName("Categoria");
        category.setSlug("categoria-" + store.getSlug());
        category.setStore(store);
        category.setCreatedAt(LocalDateTime.now());
        return category;
    }

    private Product criarProduto(Store store, Category category, String slug) {
        Product product = new Product();
        product.setName("Produto");
        product.setSlug(slug);
        product.setDescription("Descrição");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStore(store);
        product.setCategory(category);
        product.setFeatured(false);
        product.setInStock(true);
        product.setCreatedAt(LocalDateTime.now());
        return product;
    }

    private ProductImage criarImagem(Product product, String imageUrl, int position) {
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(imageUrl);
        image.setPosition(position);
        return image;
    }
}