package com.catalog.service;

import com.catalog.domain.entity.Store;
import com.catalog.domain.entity.StoreUser;
import com.catalog.domain.entity.User;
import com.catalog.domain.enums.Provider;
import com.catalog.domain.enums.Role;
import com.catalog.domain.enums.StoreTemplate;
import com.catalog.exception.BadRequestException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do StoreUserService")
class StoreUserServiceTest {

    @Mock
    private StoreUserRepository repo;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private AccessControlService access;

    @InjectMocks
    private StoreUserService storeUserService;

    @Test
    @DisplayName("Deve retornar usuário atual na loja")
    void deveRetornarUsuarioAtualNaLoja() {
        Store store = criarLoja(1L, "minha-loja");
        User user = criarUsuario(99L, "Gabriel", "gabriel@email.com");
        StoreUser storeUser = criarStoreUser(10L, store, user, Role.OWNER);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(repo.findByUserIdAndStoreIdWithUserAndStore(99L, 1L)).thenReturn(Optional.of(storeUser));

        var response = storeUserService.getCurrentUserInStore("minha-loja", 99L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getUserId()).isEqualTo(99L);
        assertThat(response.getRole()).isEqualTo(Role.OWNER);
        verify(access).checkAdminAccess(99L, 1L);
    }

    @Test
    @DisplayName("Deve listar membros da loja")
    void deveListarMembrosDaLoja() {
        Store store = criarLoja(1L, "minha-loja");
        User owner = criarUsuario(99L, "Gabriel", "gabriel@email.com");
        User admin = criarUsuario(100L, "Admin", "admin@email.com");

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(repo.findByStoreIdWithUserAndStore(1L)).thenReturn(List.of(
                criarStoreUser(10L, store, owner, Role.OWNER),
                criarStoreUser(11L, store, admin, Role.ADMIN)
        ));

        var result = storeUserService.listByStore("minha-loja", 99L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRole()).isEqualTo(Role.OWNER);
        assertThat(result.get(1).getRole()).isEqualTo(Role.ADMIN);
        verify(access).checkAdminAccess(99L, 1L);
    }

    @Test
    @DisplayName("Deve remover ADMIN da loja")
    void deveRemoverAdminDaLoja() {
        Store store = criarLoja(1L, "minha-loja");
        User admin = criarUsuario(100L, "Admin", "admin@email.com");
        StoreUser storeUser = criarStoreUser(11L, store, admin, Role.ADMIN);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(repo.findByUserIdAndStoreId(100L, 1L)).thenReturn(Optional.of(storeUser));

        storeUserService.removeUser("minha-loja", 100L, 99L);

        verify(access).checkOwnerAccess(99L, 1L);
        verify(repo).delete(storeUser);
    }

    @Test
    @DisplayName("Não deve remover OWNER da loja")
    void naoDeveRemoverOwnerDaLoja() {
        Store store = criarLoja(1L, "minha-loja");
        User owner = criarUsuario(99L, "Gabriel", "gabriel@email.com");
        StoreUser storeUser = criarStoreUser(10L, store, owner, Role.OWNER);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(repo.findByUserIdAndStoreId(99L, 1L)).thenReturn(Optional.of(storeUser));

        assertThatThrownBy(() -> storeUserService.removeUser("minha-loja", 99L, 99L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Não é possível remover o OWNER da loja.");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não pertence à loja")
    void deveLancarExcecaoQuandoUsuarioNaoPertenceALoja() {
        Store store = criarLoja(1L, "minha-loja");

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(repo.findByUserIdAndStoreIdWithUserAndStore(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeUserService.getCurrentUserInStore("minha-loja", 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Usuário não pertence à loja.");
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

    private User criarUsuario(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setProvider(Provider.GOOGLE);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private StoreUser criarStoreUser(Long id, Store store, User user, Role role) {
        StoreUser storeUser = new StoreUser();
        storeUser.setId(id);
        storeUser.setStore(store);
        storeUser.setUser(user);
        storeUser.setRole(role);
        storeUser.setCreatedAt(LocalDateTime.now());
        return storeUser;
    }
}
