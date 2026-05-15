package com.catalog.controller.admin;

import com.catalog.dto.product.ProductRequest;
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
@DisplayName("Testes do AdminProductController")
class AdminProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private AdminProductController adminProductController;

    @Test
    @DisplayName("Deve criar produto")
    void deveCriarProduto() {
        ProductRequest request = criarProdutoRequest();
        ProductResponse product = criarProdutoResponse();

        when(productService.create("minha-loja", request, 99L)).thenReturn(product);

        var response = adminProductController.create("minha-loja", request, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isEqualTo(product);
        verify(productService).create("minha-loja", request, 99L);
    }

    @Test
    @DisplayName("Deve atualizar produto")
    void deveAtualizarProduto() {
        ProductRequest request = criarProdutoRequest();
        ProductResponse product = criarProdutoResponse();

        when(productService.update("minha-loja", 1L, request, 99L)).thenReturn(product);

        var response = adminProductController.update("minha-loja", 1L, request, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(product);
        verify(productService).update("minha-loja", 1L, request, 99L);
    }

    @Test
    @DisplayName("Deve deletar produto")
    void deveDeletarProduto() {
        var response = adminProductController.delete("minha-loja", 1L, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(productService).delete("minha-loja", 1L, 99L);
    }

    @Test
    @DisplayName("Deve buscar produto por id")
    void deveBuscarProdutoPorId() {
        ProductResponse product = criarProdutoResponse();

        when(productService.getById("minha-loja", 1L, 99L)).thenReturn(product);

        var response = adminProductController.getById("minha-loja", 1L, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(product);
        verify(productService).getById("minha-loja", 1L, 99L);
    }

    @Test
    @DisplayName("Deve listar produtos administrativos paginados")
    void deveListarProdutosAdministrativosPaginados() {
        PageRequest pageable = PageRequest.of(0, 10);
        ProductResponse product = criarProdutoResponse();

        when(productService.listAdmin("minha-loja", "vestido", pageable, 99L))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        var response = adminProductController.list("minha-loja", "vestido", pageable, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
        verify(productService).listAdmin("minha-loja", "vestido", pageable, 99L);
    }

    private ProductRequest criarProdutoRequest() {
        return ProductRequest.builder()
                .name("Vestido")
                .description("Descrição")
                .price(BigDecimal.valueOf(100))
                .promotionalPrice(BigDecimal.valueOf(90))
                .categoryId(1L)
                .featured(false)
                .inStock(true)
                .build();
    }

    private ProductResponse criarProdutoResponse() {
        return ProductResponse.builder()
                .id(1L)
                .name("Vestido")
                .slug("vestido")
                .price(BigDecimal.valueOf(100))
                .categoryId(1L)
                .featured(false)
                .inStock(true)
                .build();
    }
}
