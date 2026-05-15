package com.katallo.controller.admin;

import com.katallo.domain.enums.StoreTemplate;
import com.katallo.dto.store.StoreRequest;
import com.katallo.dto.store.StoreResponse;
import com.katallo.service.StoreService;
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
@DisplayName("Testes do AdminStoreController")
class AdminStoreControllerTest {

    @Mock
    private StoreService storeService;

    @InjectMocks
    private AdminStoreController adminStoreController;

    @Test
    @DisplayName("Deve criar loja")
    void deveCriarLoja() {
        StoreRequest request = StoreRequest.builder().name("Minha Loja").template(StoreTemplate.MINIMAL).build();
        StoreResponse store = StoreResponse.builder().id(1L).name("Minha Loja").slug("minha-loja").template(StoreTemplate.MINIMAL).active(true).build();

        when(storeService.create(request, 99L)).thenReturn(store);

        var response = adminStoreController.create(request, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isEqualTo(store);
        verify(storeService).create(request, 99L);
    }

    @Test
    @DisplayName("Deve listar lojas do usuário")
    void deveListarLojasDoUsuario() {
        List<StoreResponse> stores = List.of(StoreResponse.builder().id(1L).name("Minha Loja").build());

        when(storeService.getUserStores(99L)).thenReturn(stores);

        var response = adminStoreController.getUserStores(99L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(stores);
        verify(storeService).getUserStores(99L);
    }
}
