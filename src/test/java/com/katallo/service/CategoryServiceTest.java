package com.katallo.service;

import com.katallo.domain.entity.Category;
import com.katallo.domain.entity.Store;
import com.katallo.domain.enums.StoreTemplate;
import com.katallo.dto.category.CategoryRequest;
import com.katallo.exception.ForbiddenException;
import com.katallo.exception.NotFoundException;
import com.katallo.repository.CategoryRepository;
import com.katallo.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CategoryService")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private AccessControlService access;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("Deve criar categoria com slug gerado")
    void deveCriarCategoriaComSlugGerado() {
        Store store = criarLoja(1L, "minha-loja");
        CategoryRequest request = CategoryRequest.builder().name("Vestidos Longos").build();

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(categoryRepository.existsByStoreIdAndSlug(1L, "vestidos-longos")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(10L);
            return category;
        });

        var response = categoryService.create("minha-loja", request, 99L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Vestidos Longos");
        assertThat(response.getSlug()).isEqualTo("vestidos-longos");
        assertThat(response.getStoreId()).isEqualTo(1L);
        verify(access).checkAdminAccess(99L, 1L);
    }

    @Test
    @DisplayName("Deve listar categorias administrativas da loja")
    void deveListarCategoriasAdministrativasDaLoja() {
        Store store = criarLoja(1L, "minha-loja");
        Category category = criarCategoria(10L, "Vestidos", "vestidos", store, null);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(categoryRepository.findByStoreIdAndDeletedAtIsNull(1L)).thenReturn(List.of(category));

        var result = categoryService.listByStore("minha-loja", 99L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("vestidos");
        verify(access).checkAdminAccess(99L, 1L);
    }

    @Test
    @DisplayName("Deve listar categorias públicas da loja sem validar usuário")
    void deveListarCategoriasPublicasDaLojaSemValidarUsuario() {
        Store store = criarLoja(1L, "minha-loja");
        Category category = criarCategoria(10L, "Vestidos", "vestidos", store, null);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(categoryRepository.findByStoreIdAndDeletedAtIsNull(1L)).thenReturn(List.of(category));

        var result = categoryService.listPublicByStore("minha-loja");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Vestidos");
    }

    @Test
    @DisplayName("Deve atualizar categoria da mesma loja")
    void deveAtualizarCategoriaDaMesmaLoja() {
        Store store = criarLoja(1L, "minha-loja");
        Category category = criarCategoria(10L, "Vestidos", "vestidos", store, null);
        CategoryRequest request = CategoryRequest.builder().name("Moda Feminina").build();

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByStoreIdAndSlug(1L, "moda-feminina")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = categoryService.update("minha-loja", 10L, request, 99L);

        assertThat(response.getName()).isEqualTo("Moda Feminina");
        assertThat(response.getSlug()).isEqualTo("moda-feminina");
        verify(access).checkAdminAccess(99L, 1L);
    }

    @Test
    @DisplayName("Deve negar atualização de categoria de outra loja")
    void deveNegarAtualizacaoDeCategoriaDeOutraLoja() {
        Store store = criarLoja(1L, "minha-loja");
        Store outraStore = criarLoja(2L, "outra-loja");
        Category category = criarCategoria(10L, "Vestidos", "vestidos", outraStore, null);
        CategoryRequest request = CategoryRequest.builder().name("Moda Feminina").build();

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.update("minha-loja", 10L, request, 99L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Categoria não pertence à loja.");
    }

    @Test
    @DisplayName("Deve aplicar soft delete na categoria")
    void deveAplicarSoftDeleteNaCategoria() {
        Store store = criarLoja(1L, "minha-loja");
        Category category = criarCategoria(10L, "Vestidos", "vestidos", store, null);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));

        categoryService.delete("minha-loja", 10L, 99L);

        assertThat(category.getDeletedAt()).isNotNull();
        assertThat(category.getUpdatedBy()).isEqualTo(99L);
        verify(categoryRepository).save(category);
    }

    @Test
    @DisplayName("Deve lançar exceção quando loja não existir")
    void deveLancarExcecaoQuandoLojaNaoExistir() {
        when(storeRepository.findBySlug("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.listPublicByStore("inexistente"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Loja não encontrada.");
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
}
