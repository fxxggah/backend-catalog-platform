package com.catalog.service;

import com.catalog.domain.entity.AnalyticsEvent;
import com.catalog.domain.entity.Category;
import com.catalog.domain.entity.Product;
import com.catalog.domain.entity.Store;
import com.catalog.domain.enums.AnalyticsEventType;
import com.catalog.domain.enums.StoreTemplate;
import com.catalog.dto.analytics.AnalyticsEventRequest;
import com.catalog.exception.NotFoundException;
import com.catalog.repository.AnalyticsEventRepository;
import com.catalog.repository.ProductRepository;
import com.catalog.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AnalyticsService")
class AnalyticsServiceTest {

    @Mock
    private AnalyticsEventRepository analyticsEventRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AccessControlService accessControlService;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("Deve registrar visualização da loja")
    void deveRegistrarVisualizacaoDaLoja() {
        Store store = criarLoja(1L, "minha-loja");
        AnalyticsEventRequest request = AnalyticsEventRequest.builder()
                .sessionId(" session ")
                .referrer(" instagram ")
                .userAgent(" Mozilla ")
                .build();

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(analyticsEventRepository.save(any(AnalyticsEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        analyticsService.registerStoreView("minha-loja", request);

        verify(analyticsEventRepository).save(any(AnalyticsEvent.class));
    }

    @Test
    @DisplayName("Deve registrar visualização de produto")
    void deveRegistrarVisualizacaoDeProduto() {
        Store store = criarLoja(1L, "minha-loja");
        Product product = criarProduto(10L, "Produto", "produto", store);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(productRepository.findByStoreIdAndSlugAndDeletedAtIsNull(1L, "produto"))
                .thenReturn(Optional.of(product));

        analyticsService.registerProductView("minha-loja", "produto", null);

        verify(analyticsEventRepository).save(any(AnalyticsEvent.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar visualização de produto inexistente")
    void deveLancarExcecaoAoRegistrarVisualizacaoDeProdutoInexistente() {
        Store store = criarLoja(1L, "minha-loja");

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(productRepository.findByStoreIdAndSlugAndDeletedAtIsNull(1L, "inexistente"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> analyticsService.registerProductView("minha-loja", "inexistente", null))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Produto não encontrado.");
    }

    @Test
    @DisplayName("Deve registrar clique no WhatsApp")
    void deveRegistrarCliqueNoWhatsapp() {
        Store store = criarLoja(1L, "minha-loja");
        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));

        analyticsService.registerWhatsappClick("minha-loja", null);

        verify(analyticsEventRepository).save(any(AnalyticsEvent.class));
    }

    @Test
    @DisplayName("Deve retornar resumo de analytics")
    void deveRetornarResumoDeAnalytics() {
        Store store = criarLoja(1L, "minha-loja");

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(analyticsEventRepository.countByStoreIdAndEventType(1L, AnalyticsEventType.STORE_VIEW)).thenReturn(10L);
        when(analyticsEventRepository.countByStoreIdAndEventType(1L, AnalyticsEventType.PRODUCT_VIEW)).thenReturn(20L);
        when(analyticsEventRepository.countByStoreIdAndEventType(1L, AnalyticsEventType.WHATSAPP_CLICK)).thenReturn(5L);
        when(analyticsEventRepository.findTopProductsByEventType(1L, AnalyticsEventType.PRODUCT_VIEW, PageRequest.of(0, 1)))
                .thenReturn(List.<Object[]>of(new Object[]{100L, "Produto Top", "produto-top", 7L}));

        var response = analyticsService.getSummary("minha-loja", 99L);

        assertThat(response.getStoreViews()).isEqualTo(10L);
        assertThat(response.getProductViews()).isEqualTo(20L);
        assertThat(response.getWhatsappClicks()).isEqualTo(5L);
        assertThat(response.getMostViewedProductId()).isEqualTo(100L);
        assertThat(response.getMostViewedProductName()).isEqualTo("Produto Top");
        assertThat(response.getMostViewedProductViews()).isEqualTo(7L);
        verify(accessControlService, times(2)).checkAdminAccess(99L, 1L);
    }

    @Test
    @DisplayName("Deve retornar top produtos com limite normalizado")
    void deveRetornarTopProdutosComLimiteNormalizado() {
        Store store = criarLoja(1L, "minha-loja");

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(analyticsEventRepository.findTopProductsByEventType(1L, AnalyticsEventType.PRODUCT_VIEW, PageRequest.of(0, 5)))
                .thenReturn(List.<Object[]>of(new Object[]{100L, "Produto", "produto", 3L}));

        var result = analyticsService.getTopProducts("minha-loja", 99L, 0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(100L);
        assertThat(result.get(0).getViews()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Deve retornar visitas diárias")
    void deveRetornarVisitasDiarias() {
        Store store = criarLoja(1L, "minha-loja");
        Date sqlDate = Date.valueOf(LocalDate.of(2026, 5, 15));

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(analyticsEventRepository.findDailyVisits(eq(1L), eq(AnalyticsEventType.STORE_VIEW), any(LocalDateTime.class)))
                .thenReturn(List.<Object[]>of(new Object[]{sqlDate, 12L}));

        var result = analyticsService.getDailyVisits("minha-loja", 99L, 7);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2026, 5, 15));
        assertThat(result.get(0).getVisits()).isEqualTo(12L);
        verify(accessControlService).checkAdminAccess(99L, 1L);
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

    private Product criarProduto(Long id, String name, String slug, Store store) {
        Category category = new Category();
        category.setId(20L);
        category.setStore(store);

        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setSlug(slug);
        product.setPrice(BigDecimal.valueOf(100));
        product.setStore(store);
        product.setCategory(category);
        product.setInStock(true);
        product.setFeatured(false);
        product.setCreatedAt(LocalDateTime.now());
        return product;
    }
}
