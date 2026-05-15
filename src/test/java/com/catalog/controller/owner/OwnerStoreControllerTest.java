package com.catalog.controller.owner;

import com.catalog.domain.enums.StoreTemplate;
import com.catalog.dto.store.StoreRequest;
import com.catalog.dto.store.StoreResponse;
import com.catalog.service.StoreService;
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
@DisplayName("Testes do OwnerStoreController")
class OwnerStoreControllerTest {

    @Mock
    private StoreService storeService;

    @InjectMocks
    private OwnerStoreController ownerStoreController;

    @Test
    @DisplayName("Deve atualizar loja")
    void deveAtualizarLoja() {
        StoreRequest request = StoreRequest.builder().name("Loja Atualizada").template(StoreTemplate.MINIMAL).build();
        StoreResponse store = StoreResponse.builder().id(1L).name("Loja Atualizada").slug("minha-loja").template(StoreTemplate.MINIMAL).build();

        when(storeService.update("minha-loja", request, 99L)).thenReturn(store);

        var response = ownerStoreController.update("minha-loja", request, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(store);
        verify(storeService).update("minha-loja", request, 99L);
    }

    @Test
    @DisplayName("Deve desativar loja")
    void deveDesativarLoja() {
        var response = ownerStoreController.deactivate("minha-loja", 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(storeService).deactivate("minha-loja", 99L);
    }
}
