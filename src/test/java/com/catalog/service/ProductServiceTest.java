package com.catalog.service;

import com.catalog.domain.entity.Category;
import com.catalog.domain.entity.Product;
import com.catalog.domain.entity.ProductImage;
import com.catalog.domain.entity.Store;
import com.catalog.domain.enums.StoreTemplate;
import com.catalog.dto.product.ProductRequest;
import com.catalog.exception.BadRequestException;
import com.catalog.exception.ForbiddenException;
import com.catalog.exception.NotFoundException;
import com.catalog.repository.CategoryRepository;
import com.catalog.repository.ProductRepository;
import com.catalog.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ProductService")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private AccessControlService access;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("Deve criar produto com slug gerado")
    void deveCriarProdutoComSlugGerado() {
        Store store = criarLoja(1L, "minha-loja");
        Category category = criarCategoria(10L, "Roupas", "roupas", store, null);
        ProductRequest request = criarProductRequest("Vestido Floral", 10L);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(productRepository.existsByStoreIdAndSlug(1L, "vestido-floral")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(100L);
            return product;
        });

        var response = productService.create("minha-loja", request, 99L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getName()).isEqualTo("Vestido Floral");
        assertThat(response.getSlug()).isEqualTo("vestido-floral");
        assertThat(response.getCategoryId()).isEqualTo(10L);
        assertThat(response.getInStock()).isTrue();
        assertThat(response.getFeatured()).isFalse();
        verify(access).checkAdminAccess(99L, 1L);
    }

    @Test
    @DisplayName("Deve lançar erro quando preço promocional for maior ou igual ao preço")
    void deveLancarErroQuandoPrecoPromocionalForMaiorOuIgualAoPreco() {
        Store store = criarLoja(1L, "minha-loja");
        ProductRequest request = criarProductRequest("Vestido", 10L);
        request.setPrice(BigDecimal.valueOf(100));
        request.setPromotionalPrice(BigDecimal.valueOf(100));

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> productService.create("minha-loja", request, 99L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Preço promocional inválido. Ele deve ser menor que o preço original.");
    }

    @Test
    @DisplayName("Deve lançar erro quando produto sem estoque for marcado como destaque")
    void deveLancarErroQuandoProdutoSemEstoqueForMarcadoComoDestaque() {
        Store store = criarLoja(1L, "minha-loja");
        Category category = criarCategoria(10L, "Roupas", "roupas", store, null);
        ProductRequest request = criarProductRequest("Vestido", 10L);
        request.setInStock(false);
        request.setFeatured(true);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> productService.create("minha-loja", request, 99L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Produto esgotado não pode ser marcado como destaque.");
    }

    @Test
    @DisplayName("Deve negar criação quando categoria pertence a outra loja")
    void deveNegarCriacaoQuandoCategoriaPertenceAOutraLoja() {
        Store store = criarLoja(1L, "minha-loja");
        Store outraStore = criarLoja(2L, "outra-loja");
        Category category = criarCategoria(10L, "Roupas", "roupas", outraStore, null);
        ProductRequest request = criarProductRequest("Vestido", 10L);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> productService.create("minha-loja", request, 99L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Categoria não pertence à loja.");
    }

    @Test
    @DisplayName("Deve listar produtos administrativos com busca")
    void deveListarProdutosAdministrativosComBusca() {
        Store store = criarLoja(1L, "minha-loja");
        Category category = criarCategoria(10L, "Roupas", "roupas", store, null);
        Product product = criarProduto(100L, "Vestido", "vestido", store, category, null);
        PageRequest pageable = PageRequest.of(0, 10);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(productRepository.findByStoreIdAndNameContainingIgnoreCaseAndDeletedAtIsNull(1L, "vest", pageable))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        var result = productService.listAdmin("minha-loja", "vest", pageable, 99L);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Vestido");
        verify(access).checkAdminAccess(99L, 1L);
    }

    @Test
    @DisplayName("Deve listar produtos públicos sem busca")
    void deveListarProdutosPublicosSemBusca() {
        Store store = criarLoja(1L, "minha-loja");
        Category category = criarCategoria(10L, "Roupas", "roupas", store, null);
        Product product = criarProduto(100L, "Vestido", "vestido", store, category, null);
        PageRequest pageable = PageRequest.of(0, 10);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(productRepository.findPublicByStoreIdOrderByInStockFirst(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        var result = productService.listPublic("minha-loja", null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSlug()).isEqualTo("vestido");
    }

    @Test
    @DisplayName("Deve buscar produto público por slug")
    void deveBuscarProdutoPublicoPorSlug() {
        Store store = criarLoja(1L, "minha-loja");
        Category category = criarCategoria(10L, "Roupas", "roupas", store, null);
        Product product = criarProduto(100L, "Vestido", "vestido", store, category, null);
        product.getImages().add(criarImagem(1L, product, "img2.jpg", 2));
        product.getImages().add(criarImagem(2L, product, "img1.jpg", 1));

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(productRepository.findByStoreIdAndSlugAndDeletedAtIsNull(1L, "vestido"))
                .thenReturn(Optional.of(product));

        var response = productService.getBySlug("minha-loja", "vestido");

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getImages()).hasSize(2);
        assertThat(response.getImages().get(0).getPosition()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve atualizar produto")
    void deveAtualizarProduto() {
        Store store = criarLoja(1L, "minha-loja");
        Category category = criarCategoria(10L, "Roupas", "roupas", store, null);
        Product product = criarProduto(100L, "Antigo", "antigo", store, category, null);
        ProductRequest request = criarProductRequest("Produto Novo", 10L);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(productRepository.findByStoreIdAndSlug(1L, "produto-novo")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = productService.update("minha-loja", 100L, request, 99L);

        assertThat(response.getName()).isEqualTo("Produto Novo");
        assertThat(response.getSlug()).isEqualTo("produto-novo");
        assertThat(product.getUpdatedBy()).isEqualTo(99L);
    }

    @Test
    @DisplayName("Deve aplicar soft delete no produto")
    void deveAplicarSoftDeleteNoProduto() {
        Store store = criarLoja(1L, "minha-loja");
        Category category = criarCategoria(10L, "Roupas", "roupas", store, null);
        Product product = criarProduto(100L, "Vestido", "vestido", store, category, null);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        productService.delete("minha-loja", 100L, 99L);

        assertThat(product.getDeletedAt()).isNotNull();
        assertThat(product.getUpdatedBy()).isEqualTo(99L);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Deve listar produtos relacionados")
    void deveListarProdutosRelacionados() {
        Store store = criarLoja(1L, "minha-loja");
        Category category = criarCategoria(10L, "Roupas", "roupas", store, null);
        Product atual = criarProduto(100L, "Atual", "atual", store, category, null);
        Product relacionado = criarProduto(101L, "Relacionado", "relacionado", store, category, null);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(productRepository.findByStoreIdAndSlugAndDeletedAtIsNull(1L, "atual"))
                .thenReturn(Optional.of(atual));
        when(productRepository.findByStoreIdAndCategoryIdAndIdNotAndInStockTrueAndDeletedAtIsNull(
                1L, 10L, 100L, PageRequest.of(0, 10)
        )).thenReturn(List.of(relacionado));

        var result = productService.getRelatedProducts("minha-loja", "atual", 50);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("relacionado");
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não existir")
    void deveLancarExcecaoQuandoProdutoNaoExistir() {
        Store store = criarLoja(1L, "minha-loja");

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(productRepository.findByStoreIdAndSlugAndDeletedAtIsNull(1L, "inexistente"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getBySlug("minha-loja", "inexistente"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Produto não encontrado.");
    }

    private ProductRequest criarProductRequest(String name, Long categoryId) {
        return ProductRequest.builder()
                .name(name)
                .description("Descrição")
                .price(BigDecimal.valueOf(100))
                .promotionalPrice(BigDecimal.valueOf(80))
                .categoryId(categoryId)
                .featured(false)
                .inStock(true)
                .build();
    }

    private Store criarLoja(Long id, String slug) {
        Store store = new Store();
        store.setId(id);
        store.setName("Loja " + slug);
        store.setSlug(slug);
        store.setTemplate(StoreTemplate.MINIMAL);
        store.setActive(true);
        store.setCreatedAt(LocalDateTime.now());
        return store;
    }

    private Category criarCategoria(Long id, String name, String slug, Store store, LocalDateTime deletedAt) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setSlug(slug);
        category.setStore(store);
        category.setDeletedAt(deletedAt);
        category.setCreatedAt(LocalDateTime.now());
        return category;
    }

    private Product criarProduto(Long id, String name, String slug, Store store, Category category, LocalDateTime deletedAt) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setSlug(slug);
        product.setDescription("Descrição");
        product.setPrice(BigDecimal.valueOf(100));
        product.setPromotionalPrice(BigDecimal.valueOf(80));
        product.setStore(store);
        product.setCategory(category);
        product.setInStock(true);
        product.setFeatured(false);
        product.setDeletedAt(deletedAt);
        product.setCreatedAt(LocalDateTime.now());
        return product;
    }

    private ProductImage criarImagem(Long id, Product product, String imageUrl, int position) {
        ProductImage image = new ProductImage();
        image.setId(id);
        image.setProduct(product);
        image.setImageUrl(imageUrl);
        image.setPosition(position);
        return image;
    }
}
