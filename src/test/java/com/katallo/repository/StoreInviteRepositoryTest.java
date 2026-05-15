package com.katallo.repository;

import com.katallo.domain.entity.Store;
import com.katallo.domain.entity.StoreInvite;
import com.katallo.domain.enums.StoreTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Testes do StoreInviteRepository")
class StoreInviteRepositoryTest {

    @Autowired
    private StoreInviteRepository storeInviteRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    @DisplayName("Deve encontrar convite pelo token")
    void deveEncontrarConvitePeloToken() {
        Store store = storeRepository.save(criarLoja("loja-convite-token"));
        storeInviteRepository.save(criarConvite(store, "admin@email.com", "token-123", null, LocalDateTime.now().plusDays(2)));

        var result = storeInviteRepository.findByToken("token-123");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("admin@email.com");
    }

    @Test
    @DisplayName("Deve listar convites pendentes por loja")
    void deveListarConvitesPendentesPorLoja() {
        Store store = storeRepository.save(criarLoja("loja-convites-pendentes"));

        storeInviteRepository.save(criarConvite(store, "pendente@email.com", "token-pendente", null, LocalDateTime.now().plusDays(2)));
        storeInviteRepository.save(criarConvite(store, "usado@email.com", "token-usado", LocalDateTime.now(), LocalDateTime.now().plusDays(2)));

        var result = storeInviteRepository.findByStoreIdAndUsedAtIsNull(store.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("pendente@email.com");
    }

    @Test
    @DisplayName("Deve encontrar convite pendente pelo token")
    void deveEncontrarConvitePendentePeloToken() {
        Store store = storeRepository.save(criarLoja("loja-convite-pendente-token"));
        storeInviteRepository.save(criarConvite(store, "admin@email.com", "token-pendente", null, LocalDateTime.now().plusDays(2)));

        var result = storeInviteRepository.findByTokenAndUsedAtIsNull("token-pendente");

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Não deve encontrar convite usado ao buscar por token pendente")
    void naoDeveEncontrarConviteUsadoAoBuscarPorTokenPendente() {
        Store store = storeRepository.save(criarLoja("loja-convite-usado"));
        storeInviteRepository.save(criarConvite(store, "admin@email.com", "token-usado", LocalDateTime.now(), LocalDateTime.now().plusDays(2)));

        var result = storeInviteRepository.findByTokenAndUsedAtIsNull("token-usado");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve encontrar convite pendente e não expirado pelo token")
    void deveEncontrarConvitePendenteENaoExpiradoPeloToken() {
        Store store = storeRepository.save(criarLoja("loja-convite-valido"));
        storeInviteRepository.save(criarConvite(store, "admin@email.com", "token-valido", null, LocalDateTime.now().plusDays(2)));

        var result = storeInviteRepository.findByTokenAndUsedAtIsNullAndExpiresAtAfter("token-valido", LocalDateTime.now());

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Não deve encontrar convite expirado")
    void naoDeveEncontrarConviteExpirado() {
        Store store = storeRepository.save(criarLoja("loja-convite-expirado"));
        storeInviteRepository.save(criarConvite(store, "admin@email.com", "token-expirado", null, LocalDateTime.now().minusDays(1)));

        var result = storeInviteRepository.findByTokenAndUsedAtIsNullAndExpiresAtAfter("token-expirado", LocalDateTime.now());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar se existe convite pendente por e-mail e loja")
    void deveVerificarSeExisteConvitePendentePorEmailELoja() {
        Store store = storeRepository.save(criarLoja("loja-existe-convite"));
        storeInviteRepository.save(criarConvite(store, "admin@email.com", "token-existe", null, LocalDateTime.now().plusDays(2)));

        boolean exists = storeInviteRepository.existsByEmailAndStoreIdAndUsedAtIsNull("admin@email.com", store.getId());

        assertThat(exists).isTrue();
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

    private StoreInvite criarConvite(
            Store store,
            String email,
            String token,
            LocalDateTime usedAt,
            LocalDateTime expiresAt
    ) {
        StoreInvite invite = new StoreInvite();
        invite.setStore(store);
        invite.setEmail(email);
        invite.setToken(token);
        invite.setCreatedBy(1L);
        invite.setUsedAt(usedAt);
        invite.setExpiresAt(expiresAt);
        invite.setCreatedAt(LocalDateTime.now());
        return invite;
    }
}