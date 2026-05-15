package com.catalog.repository;

import com.catalog.domain.entity.AnalyticsEvent;
import com.catalog.domain.entity.Category;
import com.catalog.domain.entity.Product;
import com.catalog.domain.entity.Store;
import com.catalog.domain.enums.AnalyticsEventType;
import com.catalog.domain.enums.StoreTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Testes do AnalyticsEventRepository")
class AnalyticsEventRepositoryTest {

    @Autowired
    private AnalyticsEventRepository analyticsEventRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Deve contar eventos por loja e tipo")
    void deveContarEventosPorLojaETipo() {
        Store store = storeRepository.save(criarLoja("loja-analytics-count"));

        analyticsEventRepository.save(criarEvento(store, null, AnalyticsEventType.STORE_VIEW, LocalDateTime.now()));
        analyticsEventRepository.save(criarEvento(store, null, AnalyticsEventType.STORE_VIEW, LocalDateTime.now()));
        analyticsEventRepository.save(criarEvento(store, null, AnalyticsEventType.WHATSAPP_CLICK, LocalDateTime.now()));

        long count = analyticsEventRepository.countByStoreIdAndEventType(store.getId(), AnalyticsEventType.STORE_VIEW);

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve contar eventos por loja, tipo e período")
    void deveContarEventosPorLojaTipoEPeriodo() {
        Store store = storeRepository.save(criarLoja("loja-analytics-periodo"));

        analyticsEventRepository.save(criarEvento(store, null, AnalyticsEventType.STORE_VIEW, LocalDateTime.now().minusDays(1)));
        analyticsEventRepository.save(criarEvento(store, null, AnalyticsEventType.STORE_VIEW, LocalDateTime.now().minusDays(10)));

        long count = analyticsEventRepository.countByStoreIdAndEventTypeAndCreatedAtBetween(
                store.getId(),
                AnalyticsEventType.STORE_VIEW,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().plusDays(1)
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve listar produtos mais visualizados por tipo de evento")
    void deveListarProdutosMaisVisualizadosPorTipoDeEvento() {
        Store store = storeRepository.save(criarLoja("loja-top-produtos"));
        Category category = categoryRepository.save(criarCategoria(store));

        Product produtoMaisVisto = productRepository.save(criarProduto("Mais Visto", "mais-visto", store, category, null));
        Product produtoMenosVisto = productRepository.save(criarProduto("Menos Visto", "menos-visto", store, category, null));
        Product produtoDeletado = productRepository.save(criarProduto("Deletado", "deletado", store, category, LocalDateTime.now()));

        analyticsEventRepository.save(criarEvento(store, produtoMaisVisto, AnalyticsEventType.PRODUCT_VIEW, LocalDateTime.now()));
        analyticsEventRepository.save(criarEvento(store, produtoMaisVisto, AnalyticsEventType.PRODUCT_VIEW, LocalDateTime.now()));
        analyticsEventRepository.save(criarEvento(store, produtoMenosVisto, AnalyticsEventType.PRODUCT_VIEW, LocalDateTime.now()));
        analyticsEventRepository.save(criarEvento(store, produtoDeletado, AnalyticsEventType.PRODUCT_VIEW, LocalDateTime.now()));

        var result = analyticsEventRepository.findTopProductsByEventType(
                store.getId(),
                AnalyticsEventType.PRODUCT_VIEW,
                PageRequest.of(0, 10)
        );

        assertThat(result).hasSize(2);

        Object[] primeiro = result.get(0);
        assertThat(primeiro[1]).isEqualTo("Mais Visto");
        assertThat(primeiro[3]).isEqualTo(2L);
    }

    private Store criarLoja(String slug) {
        Store store = new Store();
        store.setName("Loja " + slug);
        store.setSlug(slug);
        store.setTemplate(StoreTemplate.MINIMAL);
        store.setActive(true);
        store.setCreatedAt(LocalDateTime.now());
        return store;
    }

    private Category criarCategoria(Store store) {
        Category category = new Category();
        category.setName("Categoria");
        category.setSlug("categoria-" + store.getSlug());
        category.setStore(store);
        category.setCreatedAt(LocalDateTime.now());
        return category;
    }

    private Product criarProduto(String name, String slug, Store store, Category category, LocalDateTime deletedAt) {
        Product product = new Product();
        product.setName(name);
        product.setSlug(slug);
        product.setDescription("Descrição");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStore(store);
        product.setCategory(category);
        product.setFeatured(false);
        product.setInStock(true);
        product.setDeletedAt(deletedAt);
        product.setCreatedAt(LocalDateTime.now());
        return product;
    }

    private AnalyticsEvent criarEvento(
            Store store,
            Product product,
            AnalyticsEventType eventType,
            LocalDateTime createdAt
    ) {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setStore(store);
        event.setProduct(product);
        event.setEventType(eventType);
        event.setSessionId("session-123");
        event.setReferrer("instagram");
        event.setUserAgent("Mozilla");
        event.setCreatedAt(createdAt);
        return event;
    }
}