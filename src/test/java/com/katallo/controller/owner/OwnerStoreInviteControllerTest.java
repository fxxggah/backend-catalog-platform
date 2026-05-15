package com.katallo.controller.owner;

import com.katallo.dto.storeinvite.StoreInviteCreateResponse;
import com.katallo.dto.storeinvite.StoreInviteRequest;
import com.katallo.dto.storeinvite.StoreInviteResponse;
import com.katallo.service.StoreInviteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do OwnerStoreInviteController")
class OwnerStoreInviteControllerTest {

    @Mock
    private StoreInviteService storeInviteService;

    @InjectMocks
    private OwnerStoreInviteController ownerStoreInviteController;

    @Test
    @DisplayName("Deve criar convite")
    void deveCriarConvite() {
        StoreInviteRequest request = StoreInviteRequest.builder().email("admin@email.com").build();
        StoreInviteCreateResponse invite = StoreInviteCreateResponse.builder()
                .id(1L)
                .email("admin@email.com")
                .token("token")
                .expiresAt(LocalDateTime.now().plusDays(2))
                .build();

        when(storeInviteService.invite("minha-loja", request, 99L)).thenReturn(invite);

        var response = ownerStoreInviteController.invite("minha-loja", request, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isEqualTo(invite);
        verify(storeInviteService).invite("minha-loja", request, 99L);
    }

    @Test
    @DisplayName("Deve listar convites")
    void deveListarConvites() {
        List<StoreInviteResponse> invites = List.of(StoreInviteResponse.builder().id(1L).email("admin@email.com").build());

        when(storeInviteService.listByStore("minha-loja", 99L)).thenReturn(invites);

        var response = ownerStoreInviteController.list("minha-loja", 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(invites);
        verify(storeInviteService).listByStore("minha-loja", 99L);
    }

    @Test
    @DisplayName("Deve deletar convite")
    void deveDeletarConvite() {
        var response = ownerStoreInviteController.delete("minha-loja", 1L, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(storeInviteService).delete("minha-loja", 1L, 99L);
    }
}
