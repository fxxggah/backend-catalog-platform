package com.catalog.controller.viewer;

import com.catalog.dto.product.ProductResponse;
import com.catalog.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do PublicProductController")
class PublicProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private PublicProductController publicProductController;

    @Test
    @DisplayName("Deve listar produtos públicos paginados")
    void deveListarProdutosPublicosPaginados() {
        PageRequest pageable = PageRequest.of(0, 10);
        ProductResponse product = criarProdutoResponse();

        when(productService.listPublic("minha-loja", "vestido", pageable))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        var response = publicProductController.list("minha-loja", "vestido", pageable);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        verify(productService).listPublic("minha-loja", "vestido", pageable);
    }

    @Test
    @DisplayName("Deve retornar produtos em destaque")
    void deveRetornarProdutosEmDestaque() {
        List<ProductResponse> products = List.of(criarProdutoResponse());

        when(productService.getFeaturedProducts("minha-loja")).thenReturn(products);

        var response = publicProductController.getFeaturedProducts("minha-loja");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(products);
        verify(productService).getFeaturedProducts("minha-loja");
    }

    @Test
    @DisplayName("Deve retornar novos produtos")
    void deveRetornarNovosProdutos() {
        List<ProductResponse> products = List.of(criarProdutoResponse());

        when(productService.getNewArrivals("minha-loja")).thenReturn(products);

        var response = publicProductController.getNewArrivals("minha-loja");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(products);
        verify(productService).getNewArrivals("minha-loja");
    }

    @Test
    @DisplayName("Deve buscar produto público pelo slug")
    void deveBuscarProdutoPublicoPeloSlug() {
        ProductResponse product = criarProdutoResponse();

        when(productService.getBySlug("minha-loja", "vestido")).thenReturn(product);

        var response = publicProductController.getBySlug("minha-loja", "vestido");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(product);
        verify(productService).getBySlug("minha-loja", "vestido");
    }

    @Test
    @DisplayName("Deve retornar produtos relacionados")
    void deveRetornarProdutosRelacionados() {
        List<ProductResponse> products = List.of(criarProdutoResponse());

        when(productService.getRelatedProducts("minha-loja", "vestido", 10)).thenReturn(products);

        var response = publicProductController.getRelatedProducts("minha-loja", "vestido", 10);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(products);
        verify(productService).getRelatedProducts("minha-loja", "vestido", 10);
    }

    private ProductResponse criarProdutoResponse() {
        return ProductResponse.builder()
                .id(1L)
                .name("Vestido")
                .slug("vestido")
                .price(BigDecimal.valueOf(100))
                .inStock(true)
                .featured(false)
                .build();
    }
}
