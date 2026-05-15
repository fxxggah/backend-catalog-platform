package com.katallo.service;

import com.katallo.domain.entity.Store;
import com.katallo.domain.entity.StoreInvite;
import com.katallo.domain.entity.StoreUser;
import com.katallo.domain.entity.User;
import com.katallo.domain.enums.Provider;
import com.katallo.domain.enums.StoreTemplate;
import com.katallo.dto.storeinvite.StoreInviteRequest;
import com.katallo.exception.BadRequestException;
import com.katallo.exception.ConflictException;
import com.katallo.exception.ForbiddenException;
import com.katallo.repository.StoreInviteRepository;
import com.katallo.repository.StoreRepository;
import com.katallo.repository.StoreUserRepository;
import com.katallo.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do StoreInviteService")
class StoreInviteServiceTest {

    @Mock
    private StoreInviteRepository repo;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreUserRepository storeUserRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccessControlService access;

    @InjectMocks
    private StoreInviteService storeInviteService;

    @Test
    @DisplayName("Deve criar convite para loja")
    void deveCriarConviteParaLoja() {
        Store store = criarLoja(1L, "minha-loja");
        StoreInviteRequest request = StoreInviteRequest.builder().email("admin@email.com").build();

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(repo.existsByEmailAndStoreIdAndUsedAtIsNull("admin@email.com", 1L)).thenReturn(false);
        when(repo.save(any(StoreInvite.class))).thenAnswer(invocation -> {
            StoreInvite invite = invocation.getArgument(0);
            invite.setId(10L);
            return invite;
        });

        var response = storeInviteService.invite("minha-loja", request, 99L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getEmail()).isEqualTo("admin@email.com");
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getExpiresAt()).isNotNull();
        verify(access).checkOwnerAccess(99L, 1L);
    }

    @Test
    @DisplayName("Deve bloquear convite duplicado pendente")
    void deveBloquearConviteDuplicadoPendente() {
        Store store = criarLoja(1L, "minha-loja");
        StoreInviteRequest request = StoreInviteRequest.builder().email("admin@email.com").build();

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(repo.existsByEmailAndStoreIdAndUsedAtIsNull("admin@email.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> storeInviteService.invite("minha-loja", request, 99L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Já existe um convite pendente para este email.");
    }

    @Test
    @DisplayName("Deve aceitar convite válido")
    void deveAceitarConviteValido() {
        Store store = criarLoja(1L, "minha-loja");
        StoreInvite invite = criarConvite(10L, store, "admin@email.com", "token", null);
        User user = criarUsuario(50L, "Admin", "admin@email.com");

        when(repo.findByTokenAndUsedAtIsNullAndExpiresAtAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(invite));
        when(userRepository.findById(50L)).thenReturn(Optional.of(user));
        when(storeUserRepository.existsByUserIdAndStoreId(50L, 1L)).thenReturn(false);

        storeInviteService.accept("token", 50L);

        assertThat(invite.getUsedAt()).isNotNull();
        verify(storeUserRepository).save(any(StoreUser.class));
        verify(repo).save(invite);
    }

    @Test
    @DisplayName("Deve negar aceite quando e-mail do usuário for diferente do convite")
    void deveNegarAceiteQuandoEmailDoUsuarioForDiferenteDoConvite() {
        Store store = criarLoja(1L, "minha-loja");
        StoreInvite invite = criarConvite(10L, store, "admin@email.com", "token", null);
        User user = criarUsuario(50L, "Outro", "outro@email.com");

        when(repo.findByTokenAndUsedAtIsNullAndExpiresAtAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(invite));
        when(userRepository.findById(50L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> storeInviteService.accept("token", 50L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Este convite pertence a outro email.");
    }

    @Test
    @DisplayName("Deve bloquear aceite quando usuário já pertence à loja")
    void deveBloquearAceiteQuandoUsuarioJaPertenceALoja() {
        Store store = criarLoja(1L, "minha-loja");
        StoreInvite invite = criarConvite(10L, store, "admin@email.com", "token", null);
        User user = criarUsuario(50L, "Admin", "admin@email.com");

        when(repo.findByTokenAndUsedAtIsNullAndExpiresAtAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(invite));
        when(userRepository.findById(50L)).thenReturn(Optional.of(user));
        when(storeUserRepository.existsByUserIdAndStoreId(50L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> storeInviteService.accept("token", 50L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Usuário já pertence à loja.");
    }

    @Test
    @DisplayName("Deve listar convites pendentes da loja")
    void deveListarConvitesPendentesDaLoja() {
        Store store = criarLoja(1L, "minha-loja");
        StoreInvite invite = criarConvite(10L, store, "admin@email.com", "token", null);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(repo.findByStoreIdAndUsedAtIsNull(1L)).thenReturn(List.of(invite));

        var result = storeInviteService.listByStore("minha-loja", 99L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("admin@email.com");
        verify(access).checkOwnerAccess(99L, 1L);
    }

    @Test
    @DisplayName("Deve deletar convite da mesma loja")
    void deveDeletarConviteDaMesmaLoja() {
        Store store = criarLoja(1L, "minha-loja");
        StoreInvite invite = criarConvite(10L, store, "admin@email.com", "token", null);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(repo.findById(10L)).thenReturn(Optional.of(invite));

        storeInviteService.delete("minha-loja", 10L, 99L);

        verify(access).checkOwnerAccess(99L, 1L);
        verify(repo).delete(invite);
    }

    @Test
    @DisplayName("Deve validar token de convite")
    void deveValidarTokenDeConvite() {
        Store store = criarLoja(1L, "minha-loja");
        StoreInvite invite = criarConvite(10L, store, "admin@email.com", "token", null);

        when(repo.findByTokenAndUsedAtIsNullAndExpiresAtAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(invite));

        var response = storeInviteService.validateToken("token");

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getEmail()).isEqualTo("admin@email.com");
    }

    @Test
    @DisplayName("Deve lançar erro ao validar token inválido")
    void deveLancarErroAoValidarTokenInvalido() {
        when(repo.findByTokenAndUsedAtIsNullAndExpiresAtAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeInviteService.validateToken("token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Convite inválido ou expirado.");
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

    private StoreInvite criarConvite(Long id, Store store, String email, String token, LocalDateTime usedAt) {
        StoreInvite invite = new StoreInvite();
        invite.setId(id);
        invite.setStore(store);
        invite.setEmail(email);
        invite.setToken(token);
        invite.setCreatedBy(99L);
        invite.setExpiresAt(LocalDateTime.now().plusHours(24));
        invite.setUsedAt(usedAt);
        invite.setCreatedAt(LocalDateTime.now());
        return invite;
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
}
