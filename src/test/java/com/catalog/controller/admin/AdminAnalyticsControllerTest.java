package com.catalog.controller.admin;

import com.catalog.dto.analytics.AnalyticsSummaryResponse;
import com.catalog.dto.analytics.DailyVisitsResponse;
import com.catalog.dto.analytics.TopProductAnalyticsResponse;
import com.catalog.service.AnalyticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AdminAnalyticsController")
class AdminAnalyticsControllerTest {

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AdminAnalyticsController adminAnalyticsController;

    @Test
    @DisplayName("Deve retornar resumo de analytics")
    void deveRetornarResumoDeAnalytics() {
        AnalyticsSummaryResponse summary = AnalyticsSummaryResponse.builder()
                .storeViews(10)
                .productViews(20)
                .whatsappClicks(5)
                .mostViewedProductId(1L)
                .mostViewedProductName("Produto")
                .mostViewedProductViews(7)
                .build();

        when(analyticsService.getSummary("minha-loja", 99L)).thenReturn(summary);

        var response = adminAnalyticsController.getSummary("minha-loja", 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(summary);
        verify(analyticsService).getSummary("minha-loja", 99L);
    }

    @Test
    @DisplayName("Deve retornar produtos mais visualizados")
    void deveRetornarProdutosMaisVisualizados() {
        List<TopProductAnalyticsResponse> products = List.of(
                TopProductAnalyticsResponse.builder().productId(1L).productName("Produto").productSlug("produto").views(3).build()
        );

        when(analyticsService.getTopProducts("minha-loja", 99L, 5)).thenReturn(products);

        var response = adminAnalyticsController.getTopProducts("minha-loja", 5, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(products);
        verify(analyticsService).getTopProducts("minha-loja", 99L, 5);
    }

    @Test
    @DisplayName("Deve retornar visitas diárias")
    void deveRetornarVisitasDiarias() {
        List<DailyVisitsResponse> visits = List.of(
                DailyVisitsResponse.builder().date(LocalDate.of(2026, 5, 15)).visits(12).build()
        );

        when(analyticsService.getDailyVisits("minha-loja", 99L, 7)).thenReturn(visits);

        var response = adminAnalyticsController.getDailyVisits("minha-loja", 7, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(visits);
        verify(analyticsService).getDailyVisits("minha-loja", 99L, 7);
    }
}
