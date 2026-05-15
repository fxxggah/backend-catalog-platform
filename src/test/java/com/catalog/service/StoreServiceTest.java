package com.catalog.service;

import com.catalog.domain.entity.Store;
import com.catalog.domain.entity.StoreUser;
import com.catalog.domain.enums.Role;
import com.catalog.domain.enums.StoreTemplate;
import com.catalog.dto.store.StoreRequest;
import com.catalog.exception.NotFoundException;
import com.catalog.repository.StoreRepository;
import com.catalog.repository.StoreUserRepository;
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
@DisplayName("Testes do StoreService")
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreUserRepository storeUserRepository;

    @Mock
    private AccessControlService access;

    @InjectMocks
    private StoreService storeService;

    @Test
    @DisplayName("Deve criar loja e vincular usuário como OWNER")
    void deveCriarLojaEVincularUsuarioComoOwner() {
        StoreRequest request = criarStoreRequest("Loja da Tia", "@lojadatia");

        when(storeRepository.existsBySlug("loja-da-tia")).thenReturn(false);
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> {
            Store store = invocation.getArgument(0);
            store.setId(1L);
            return store;
        });
        when(storeUserRepository.save(any(StoreUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = storeService.create(request, 99L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getSlug()).isEqualTo("loja-da-tia");
        assertThat(response.getInstagram()).isEqualTo("lojadatia");
        assertThat(response.getActive()).isTrue();
        verify(storeUserRepository).save(any(StoreUser.class));
    }

    @Test
    @DisplayName("Deve buscar loja pelo slug")
    void deveBuscarLojaPeloSlug() {
        Store store = criarLoja(1L, "minha-loja", "Minha Loja", true);
        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));

        var response = storeService.getBySlug("minha-loja");

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getSlug()).isEqualTo("minha-loja");
    }

    @Test
    @DisplayName("Deve listar lojas do usuário")
    void deveListarLojasDoUsuario() {
        Store store = criarLoja(1L, "minha-loja", "Minha Loja", true);
        StoreUser storeUser = new StoreUser();
        storeUser.setStore(store);
        storeUser.setRole(Role.OWNER);

        when(storeUserRepository.findByUserIdWithStore(99L)).thenReturn(List.of(storeUser));

        var result = storeService.getUserStores(99L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("minha-loja");
    }

    @Test
    @DisplayName("Deve atualizar loja e regenerar slug quando nome mudar")
    void deveAtualizarLojaERegenerarSlugQuandoNomeMudar() {
        Store store = criarLoja(1L, "loja-antiga", "Loja Antiga", true);
        StoreRequest request = criarStoreRequest("Loja Nova", "@nova");

        when(storeRepository.findBySlug("loja-antiga")).thenReturn(Optional.of(store));
        when(storeRepository.existsBySlugAndIdNot("loja-nova", 1L)).thenReturn(false);
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = storeService.update("loja-antiga", request, 99L);

        assertThat(response.getName()).isEqualTo("Loja Nova");
        assertThat(response.getSlug()).isEqualTo("loja-nova");
        assertThat(response.getInstagram()).isEqualTo("nova");
        verify(access).checkOwnerAccess(99L, 1L);
    }

    @Test
    @DisplayName("Deve desativar loja")
    void deveDesativarLoja() {
        Store store = criarLoja(1L, "minha-loja", "Minha Loja", true);
        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));

        storeService.deactivate("minha-loja", 99L);

        assertThat(store.getActive()).isFalse();
        assertThat(store.getUpdatedBy()).isEqualTo(99L);
        verify(access).checkOwnerAccess(99L, 1L);
        verify(storeRepository).save(store);
    }

    @Test
    @DisplayName("Deve ativar loja")
    void deveAtivarLoja() {
        Store store = criarLoja(1L, "minha-loja", "Minha Loja", false);
        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));

        storeService.activate("minha-loja", 99L);

        assertThat(store.getActive()).isTrue();
        assertThat(store.getUpdatedBy()).isEqualTo(99L);
        verify(access).checkOwnerAccess(99L, 1L);
        verify(storeRepository).save(store);
    }

    @Test
    @DisplayName("Deve lançar exceção quando loja não existir")
    void deveLancarExcecaoQuandoLojaNaoExistir() {
        when(storeRepository.findBySlug("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeService.getBySlug("inexistente"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Loja não encontrada.");
    }

    private StoreRequest criarStoreRequest(String name, String instagram) {
        return StoreRequest.builder()
                .name(name)
                .logo("logo.png")
                .favicon("favicon.ico")
                .whatsappNumber("5514999999999")
                .instagram(instagram)
                .facebook("facebook")
                .template(StoreTemplate.MINIMAL)
                .city("Botucatu")
                .state("SP")
                .country("Brasil")
                .build();
    }

    private Store criarLoja(Long id, String slug, String name, boolean active) {
        Store store = new Store();
        store.setId(id);
        store.setName(name);
        store.setSlug(slug);
        store.setTemplate(StoreTemplate.MINIMAL);
        store.setActive(active);
        store.setCreatedAt(LocalDateTime.now());
        return store;
    }
}
