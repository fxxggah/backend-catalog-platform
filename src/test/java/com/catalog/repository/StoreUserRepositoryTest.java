package com.catalog.repository;

import com.catalog.domain.entity.Store;
import com.catalog.domain.entity.StoreUser;
import com.catalog.domain.entity.User;
import com.catalog.domain.enums.Provider;
import com.catalog.domain.enums.Role;
import com.catalog.domain.enums.StoreTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Testes do StoreUserRepository")
class StoreUserRepositoryTest {

    @Autowired
    private StoreUserRepository storeUserRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Deve encontrar vínculo por usuário e loja")
    void deveEncontrarVinculoPorUsuarioELoja() {
        User user = userRepository.save(criarUsuario("Gabriel", "gabriel@email.com"));
        Store store = storeRepository.save(criarLoja("loja-vinculo"));
        storeUserRepository.save(criarStoreUser(store, user, Role.OWNER));

        var result = storeUserRepository.findByUserIdAndStoreId(user.getId(), store.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getRole()).isEqualTo(Role.OWNER);
    }

    @Test
    @DisplayName("Deve listar vínculos por loja")
    void deveListarVinculosPorLoja() {
        User user1 = userRepository.save(criarUsuario("Gabriel", "gabriel1@email.com"));
        User user2 = userRepository.save(criarUsuario("Admin", "admin1@email.com"));
        Store store = storeRepository.save(criarLoja("loja-listar-vinculos"));

        storeUserRepository.save(criarStoreUser(store, user1, Role.OWNER));
        storeUserRepository.save(criarStoreUser(store, user2, Role.ADMIN));

        var result = storeUserRepository.findByStoreId(store.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Deve verificar se existe vínculo por usuário e loja")
    void deveVerificarSeExisteVinculoPorUsuarioELoja() {
        User user = userRepository.save(criarUsuario("Gabriel", "gabriel2@email.com"));
        Store store = storeRepository.save(criarLoja("loja-existe-vinculo"));
        storeUserRepository.save(criarStoreUser(store, user, Role.OWNER));

        boolean exists = storeUserRepository.existsByUserIdAndStoreId(user.getId(), store.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve listar vínculos por usuário")
    void deveListarVinculosPorUsuario() {
        User user = userRepository.save(criarUsuario("Gabriel", "gabriel3@email.com"));
        Store store1 = storeRepository.save(criarLoja("loja-usuario-1"));
        Store store2 = storeRepository.save(criarLoja("loja-usuario-2"));

        storeUserRepository.save(criarStoreUser(store1, user, Role.OWNER));
        storeUserRepository.save(criarStoreUser(store2, user, Role.ADMIN));

        var result = storeUserRepository.findByUserId(user.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Deve listar vínculos do usuário carregando loja com fetch join")
    void deveListarVinculosDoUsuarioCarregandoLojaComFetchJoin() {
        User user = userRepository.save(criarUsuario("Gabriel", "gabriel4@email.com"));
        Store store = storeRepository.save(criarLoja("loja-fetch-store"));
        storeUserRepository.save(criarStoreUser(store, user, Role.OWNER));

        var result = storeUserRepository.findByUserIdWithStore(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStore().getSlug()).isEqualTo("loja-fetch-store");
    }

    @Test
    @DisplayName("Deve listar membros da loja carregando usuário e loja com fetch join")
    void deveListarMembrosDaLojaCarregandoUsuarioELojaComFetchJoin() {
        User user = userRepository.save(criarUsuario("Gabriel", "gabriel5@email.com"));
        Store store = storeRepository.save(criarLoja("loja-fetch-user-store"));
        storeUserRepository.save(criarStoreUser(store, user, Role.OWNER));

        var result = storeUserRepository.findByStoreIdWithUserAndStore(store.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getEmail()).isEqualTo("gabriel5@email.com");
        assertThat(result.get(0).getStore().getSlug()).isEqualTo("loja-fetch-user-store");
    }

    @Test
    @DisplayName("Deve encontrar vínculo por usuário e loja carregando usuário e loja com fetch join")
    void deveEncontrarVinculoPorUsuarioELojaCarregandoUsuarioELojaComFetchJoin() {
        User user = userRepository.save(criarUsuario("Gabriel", "gabriel6@email.com"));
        Store store = storeRepository.save(criarLoja("loja-fetch-vinculo"));
        storeUserRepository.save(criarStoreUser(store, user, Role.OWNER));

        var result = storeUserRepository.findByUserIdAndStoreIdWithUserAndStore(user.getId(), store.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getEmail()).isEqualTo("gabriel6@email.com");
        assertThat(result.get().getStore().getSlug()).isEqualTo("loja-fetch-vinculo");
    }

    private User criarUsuario(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setProvider(Provider.GOOGLE);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
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

    private StoreUser criarStoreUser(Store store, User user, Role role) {
        StoreUser storeUser = new StoreUser();
        storeUser.setStore(store);
        storeUser.setUser(user);
        storeUser.setRole(role);
        storeUser.setCreatedAt(LocalDateTime.now());
        return storeUser;
    }
}