package com.katallo.controller.viewer;

import com.katallo.dto.category.CategoryResponse;
import com.katallo.dto.product.ProductResponse;
import com.katallo.service.CategoryService;
import com.katallo.service.ProductService;
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
@DisplayName("Testes do PublicCategoryController")
class PublicCategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private PublicCategoryController publicCategoryController;

    @Test
    @DisplayName("Deve listar categorias públicas")
    void deveListarCategoriasPublicas() {
        List<CategoryResponse> categories = List.of(CategoryResponse.builder().id(1L).name("Vestidos").build());

        when(categoryService.listPublicByStore("minha-loja")).thenReturn(categories);

        var response = publicCategoryController.listCategories("minha-loja");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(categories);
        verify(categoryService).listPublicByStore("minha-loja");
    }

    @Test
    @DisplayName("Deve listar produtos públicos por categoria")
    void deveListarProdutosPublicosPorCategoria() {
        PageRequest pageable = PageRequest.of(0, 10);
        ProductResponse product = ProductResponse.builder().id(1L).name("Vestido").price(BigDecimal.valueOf(100)).build();

        when(productService.listByCategory("minha-loja", "vestidos", pageable))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        var response = publicCategoryController.listProductsByCategory("minha-loja", "vestidos", pageable);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        verify(productService).listByCategory("minha-loja", "vestidos", pageable);
    }
}
