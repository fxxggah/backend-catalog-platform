package com.katallo.controller.viewer;

import com.katallo.domain.enums.StoreTemplate;
import com.katallo.dto.store.StoreResponse;
import com.katallo.service.StoreService;
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
@DisplayName("Testes do PublicStoreController")
class PublicStoreControllerTest {

    @Mock
    private StoreService storeService;

    @InjectMocks
    private PublicStoreController publicStoreController;

    @Test
    @DisplayName("Deve buscar loja pública pelo slug")
    void deveBuscarLojaPublicaPeloSlug() {
        StoreResponse store = StoreResponse.builder()
                .id(1L)
                .name("Minha Loja")
                .slug("minha-loja")
                .template(StoreTemplate.MINIMAL)
                .active(true)
                .build();

        when(storeService.getBySlug("minha-loja")).thenReturn(store);

        var response = publicStoreController.getBySlug("minha-loja");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(store);
        verify(storeService).getBySlug("minha-loja");
    }
}
