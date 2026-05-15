package com.katallo.controller.admin;

import com.katallo.dto.category.CategoryRequest;
import com.katallo.dto.category.CategoryResponse;
import com.katallo.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AdminCategoryController")
class AdminCategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private AdminCategoryController adminCategoryController;

    @Test
    @DisplayName("Deve criar categoria")
    void deveCriarCategoria() {
        CategoryRequest request = CategoryRequest.builder().name("Vestidos").build();
        CategoryResponse category = CategoryResponse.builder().id(1L).name("Vestidos").slug("vestidos").storeId(10L).build();

        when(categoryService.create("minha-loja", request, 99L)).thenReturn(category);

        var response = adminCategoryController.create("minha-loja", request, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isEqualTo(category);
        verify(categoryService).create("minha-loja", request, 99L);
    }

    @Test
    @DisplayName("Deve listar categorias")
    void deveListarCategorias() {
        List<CategoryResponse> categories = List.of(CategoryResponse.builder().id(1L).name("Vestidos").build());

        when(categoryService.listByStore("minha-loja", 99L)).thenReturn(categories);

        var response = adminCategoryController.list("minha-loja", 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(categories);
        verify(categoryService).listByStore("minha-loja", 99L);
    }

    @Test
    @DisplayName("Deve atualizar categoria")
    void deveAtualizarCategoria() {
        CategoryRequest request = CategoryRequest.builder().name("Roupas").build();
        CategoryResponse category = CategoryResponse.builder().id(1L).name("Roupas").slug("roupas").build();

        when(categoryService.update("minha-loja", 1L, request, 99L)).thenReturn(category);

        var response = adminCategoryController.update("minha-loja", 1L, request, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(category);
        verify(categoryService).update("minha-loja", 1L, request, 99L);
    }

    @Test
    @DisplayName("Deve deletar categoria")
    void deveDeletarCategoria() {
        var response = adminCategoryController.delete("minha-loja", 1L, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(categoryService).delete("minha-loja", 1L, 99L);
    }
}
