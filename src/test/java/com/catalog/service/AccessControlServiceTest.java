package com.catalog.service;

import com.catalog.domain.entity.StoreUser;
import com.catalog.domain.enums.Role;
import com.catalog.exception.ForbiddenException;
import com.catalog.repository.StoreUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AccessControlService")
class AccessControlServiceTest {

    @Mock
    private StoreUserRepository storeUserRepository;

    @InjectMocks
    private AccessControlService accessControlService;

    @Test
    @DisplayName("Deve retornar vínculo do usuário com a loja quando existir")
    void deveRetornarVinculoDoUsuarioComALojaQuandoExistir() {
        StoreUser storeUser = new StoreUser();
        storeUser.setRole(Role.ADMIN);

        when(storeUserRepository.findByUserIdAndStoreId(1L, 10L))
                .thenReturn(Optional.of(storeUser));

        StoreUser result = accessControlService.getStoreUserOrThrow(1L, 10L);

        assertThat(result).isSameAs(storeUser);
        verify(storeUserRepository).findByUserIdAndStoreId(1L, 10L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não pertence à loja")
    void deveLancarExcecaoQuandoUsuarioNaoPertenceALoja() {
        when(storeUserRepository.findByUserIdAndStoreId(1L, 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> accessControlService.getStoreUserOrThrow(1L, 10L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Acesso negado à loja.");
    }

    @Test
    @DisplayName("Deve permitir acesso de OWNER")
    void devePermitirAcessoDeOwner() {
        StoreUser storeUser = new StoreUser();
        storeUser.setRole(Role.OWNER);

        when(storeUserRepository.findByUserIdAndStoreId(1L, 10L))
                .thenReturn(Optional.of(storeUser));

        assertThatCode(() -> accessControlService.checkOwnerAccess(1L, 10L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve negar acesso de ADMIN em ação exclusiva de OWNER")
    void deveNegarAcessoDeAdminEmAcaoExclusivaDeOwner() {
        StoreUser storeUser = new StoreUser();
        storeUser.setRole(Role.ADMIN);

        when(storeUserRepository.findByUserIdAndStoreId(1L, 10L))
                .thenReturn(Optional.of(storeUser));

        assertThatThrownBy(() -> accessControlService.checkOwnerAccess(1L, 10L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Acesso negado. Esta ação requer permissão de OWNER.");
    }

    @Test
    @DisplayName("Deve permitir acesso administrativo para ADMIN")
    void devePermitirAcessoAdministrativoParaAdmin() {
        StoreUser storeUser = new StoreUser();
        storeUser.setRole(Role.ADMIN);

        when(storeUserRepository.findByUserIdAndStoreId(1L, 10L))
                .thenReturn(Optional.of(storeUser));

        assertThatCode(() -> accessControlService.checkAdminAccess(1L, 10L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve permitir acesso administrativo para OWNER")
    void devePermitirAcessoAdministrativoParaOwner() {
        StoreUser storeUser = new StoreUser();
        storeUser.setRole(Role.OWNER);

        when(storeUserRepository.findByUserIdAndStoreId(1L, 10L))
                .thenReturn(Optional.of(storeUser));

        assertThatCode(() -> accessControlService.checkAdminAccess(1L, 10L))
                .doesNotThrowAnyException();
    }
}
