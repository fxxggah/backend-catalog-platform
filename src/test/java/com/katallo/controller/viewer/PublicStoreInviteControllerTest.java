package com.katallo.controller.viewer;

import com.katallo.dto.storeinvite.StoreInviteResponse;
import com.katallo.service.StoreInviteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do PublicStoreInviteController")
class PublicStoreInviteControllerTest {

    @Mock
    private StoreInviteService storeInviteService;

    @InjectMocks
    private PublicStoreInviteController publicStoreInviteController;

    @Test
    @DisplayName("Deve validar convite pelo token")
    void deveValidarConvitePeloToken() {
        StoreInviteResponse invite = StoreInviteResponse.builder().id(1L).email("admin@email.com").build();

        when(storeInviteService.validateToken("token")).thenReturn(invite);

        var response = publicStoreInviteController.validate("token");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(invite);
        verify(storeInviteService).validateToken("token");
    }

    @Test
    @DisplayName("Deve aceitar convite")
    void deveAceitarConvite() {
        var response = publicStoreInviteController.accept("token", 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(storeInviteService).accept("token", 99L);
    }
}
