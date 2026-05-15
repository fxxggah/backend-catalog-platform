package com.catalog.controller.viewer;

import com.catalog.dto.analytics.AnalyticsEventRequest;
import com.catalog.service.AnalyticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do PublicAnalyticsController")
class PublicAnalyticsControllerTest {

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private PublicAnalyticsController publicAnalyticsController;

    @Test
    @DisplayName("Deve registrar visualização da loja")
    void deveRegistrarVisualizacaoDaLoja() {
        AnalyticsEventRequest request = AnalyticsEventRequest.builder().sessionId("session").build();

        var response = publicAnalyticsController.registerStoreView("minha-loja", request);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(analyticsService).registerStoreView("minha-loja", request);
    }

    @Test
    @DisplayName("Deve registrar clique no WhatsApp")
    void deveRegistrarCliqueNoWhatsapp() {
        AnalyticsEventRequest request = AnalyticsEventRequest.builder().sessionId("session").build();

        var response = publicAnalyticsController.registerWhatsappClick("minha-loja", request);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(analyticsService).registerWhatsappClick("minha-loja", request);
    }

    @Test
    @DisplayName("Deve registrar visualização de produto")
    void deveRegistrarVisualizacaoDeProduto() {
        AnalyticsEventRequest request = AnalyticsEventRequest.builder().sessionId("session").build();

        var response = publicAnalyticsController.registerProductView("minha-loja", "produto", request);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(analyticsService).registerProductView("minha-loja", "produto", request);
    }
}
