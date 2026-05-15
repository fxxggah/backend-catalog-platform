package com.catalog.controller.owner;

import com.catalog.domain.enums.Role;
import com.catalog.dto.storeuser.StoreUserResponse;
import com.catalog.service.StoreUserService;
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
@DisplayName("Testes do OwnerStoreUserController")
class OwnerStoreUserControllerTest {

    @Mock
    private StoreUserService storeUserService;

    @InjectMocks
    private OwnerStoreUserController ownerStoreUserController;

    @Test
    @DisplayName("Deve retornar usuário atual na loja")
    void deveRetornarUsuarioAtualNaLoja() {
        StoreUserResponse user = StoreUserResponse.builder().id(1L).userId(99L).storeId(10L).name("Gabriel").role(Role.OWNER).build();

        when(storeUserService.getCurrentUserInStore("minha-loja", 99L)).thenReturn(user);

        var response = ownerStoreUserController.me("minha-loja", 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(user);
        verify(storeUserService).getCurrentUserInStore("minha-loja", 99L);
    }

    @Test
    @DisplayName("Deve listar usuários da loja")
    void deveListarUsuariosDaLoja() {
        List<StoreUserResponse> users = List.of(StoreUserResponse.builder().id(1L).userId(99L).role(Role.OWNER).build());

        when(storeUserService.listByStore("minha-loja", 99L)).thenReturn(users);

        var response = ownerStoreUserController.list("minha-loja", 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(users);
        verify(storeUserService).listByStore("minha-loja", 99L);
    }

    @Test
    @DisplayName("Deve remover usuário da loja")
    void deveRemoverUsuarioDaLoja() {
        var response = ownerStoreUserController.removeUser("minha-loja", 55L, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(storeUserService).removeUser("minha-loja", 55L, 99L);
    }
}
