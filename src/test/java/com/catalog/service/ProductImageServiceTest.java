package com.catalog.service;

import com.catalog.domain.entity.Category;
import com.catalog.domain.entity.Product;
import com.catalog.domain.entity.ProductImage;
import com.catalog.domain.entity.Store;
import com.catalog.domain.enums.StoreTemplate;
import com.catalog.dto.productimage.ProductImageReorderRequest;
import com.catalog.dto.productimage.UploadImageRequest;
import com.catalog.exception.BadRequestException;
import com.catalog.exception.ForbiddenException;
import com.catalog.repository.ProductImageRepository;
import com.catalog.repository.ProductRepository;
import com.catalog.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ProductImageService")
class ProductImageServiceTest {

    @Mock
    private ProductImageRepository repo;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private AccessControlService access;

    @InjectMocks
    private ProductImageService productImageService;

    @Test
    @DisplayName("Deve fazer upload da imagem do produto")
    void deveFazerUploadDaImagemDoProduto() {
        Store store = criarLoja(1L, "minha-loja");
        Product product = criarProduto(10L, store);
        var file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", "abc".getBytes());
        UploadImageRequest request = UploadImageRequest.builder().productId(10L).file(file).build();

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(repo.findByProductIdOrderByPositionAsc(10L)).thenReturn(List.of());
        when(cloudinaryService.uploadImage(file)).thenReturn("https://img.com/foto.jpg");
        when(repo.save(any(ProductImage.class))).thenAnswer(invocation -> {
            ProductImage image = invocation.getArgument(0);
            image.setId(100L);
            return image;
        });

        var response = productImageService.upload("minha-loja", request, 99L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getImageUrl()).isEqualTo("https://img.com/foto.jpg");
        assertThat(response.getPosition()).isEqualTo(1);
        verify(access).checkAdminAccess(99L, 1L);
    }

    @Test
    @DisplayName("Deve bloquear upload quando produto já possui oito imagens")
    void deveBloquearUploadQuandoProdutoJaPossuiOitoImagens() {
        Store store = criarLoja(1L, "minha-loja");
        Product product = criarProduto(10L, store);
        UploadImageRequest request = UploadImageRequest.builder()
                .productId(10L)
                .file(new MockMultipartFile("file", "foto.jpg", "image/jpeg", "abc".getBytes()))
                .build();

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(repo.findByProductIdOrderByPositionAsc(10L)).thenReturn(List.of(
                new ProductImage(), new ProductImage(), new ProductImage(), new ProductImage(),
                new ProductImage(), new ProductImage(), new ProductImage(), new ProductImage()
        ));

        assertThatThrownBy(() -> productImageService.upload("minha-loja", request, 99L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Limite de imagens atingido. O máximo permitido é 8 imagens por produto.");
    }

    @Test
    @DisplayName("Deve listar imagens do produto")
    void deveListarImagensDoProduto() {
        Store store = criarLoja(1L, "minha-loja");
        Product product = criarProduto(10L, store);
        ProductImage image = criarImagem(100L, product, "img.jpg", 1);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(repo.findByProductIdOrderByPositionAsc(10L)).thenReturn(List.of(image));

        var result = productImageService.getByProduct("minha-loja", 10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getImageUrl()).isEqualTo("img.jpg");
    }

    @Test
    @DisplayName("Deve negar acesso quando produto pertence a outra loja")
    void deveNegarAcessoQuandoProdutoPertenceAOutraLoja() {
        Store store = criarLoja(1L, "minha-loja");
        Store outraStore = criarLoja(2L, "outra-loja");
        Product product = criarProduto(10L, outraStore);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productImageService.getByProduct("minha-loja", 10L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Produto não pertence à loja.");
    }

    @Test
    @DisplayName("Deve reordenar imagens do produto")
    void deveReordenarImagensDoProduto() {
        Store store = criarLoja(1L, "minha-loja");
        Product product = criarProduto(10L, store);
        ProductImage image1 = criarImagem(100L, product, "1.jpg", 1);
        ProductImage image2 = criarImagem(200L, product, "2.jpg", 2);
        ProductImageReorderRequest request = new ProductImageReorderRequest();
        request.setImageIds(List.of(200L, 100L));

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(repo.findByProductIdOrderByPositionAsc(10L)).thenReturn(List.of(image1, image2));

        productImageService.reorder("minha-loja", 10L, request, 99L);

        assertThat(image2.getPosition()).isEqualTo(1);
        assertThat(image1.getPosition()).isEqualTo(2);
        verify(repo).saveAll(List.of(image1, image2));
    }

    @Test
    @DisplayName("Deve lançar erro quando lista de reordenação for inválida")
    void deveLancarErroQuandoListaDeReordenacaoForInvalida() {
        Store store = criarLoja(1L, "minha-loja");
        Product product = criarProduto(10L, store);
        ProductImage image1 = criarImagem(100L, product, "1.jpg", 1);
        ProductImageReorderRequest request = new ProductImageReorderRequest();
        request.setImageIds(List.of(100L, 200L));

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(repo.findByProductIdOrderByPositionAsc(10L)).thenReturn(List.of(image1));

        assertThatThrownBy(() -> productImageService.reorder("minha-loja", 10L, request, 99L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Lista inválida para reordenação.");
    }

    @Test
    @DisplayName("Deve deletar imagem e reorganizar posições")
    void deveDeletarImagemEReorganizarPosicoes() {
        Store store = criarLoja(1L, "minha-loja");
        Product product = criarProduto(10L, store);
        ProductImage imageToDelete = criarImagem(100L, product, "1.jpg", 1);
        ProductImage remaining = criarImagem(200L, product, "2.jpg", 2);

        when(storeRepository.findBySlug("minha-loja")).thenReturn(Optional.of(store));
        when(repo.findById(100L)).thenReturn(Optional.of(imageToDelete));
        when(repo.findByProductIdOrderByPositionAsc(10L)).thenReturn(new ArrayList<>(List.of(remaining)));

        productImageService.delete("minha-loja", 100L, 99L);

        assertThat(remaining.getPosition()).isEqualTo(1);
        verify(repo).delete(imageToDelete);
        verify(repo).saveAll(List.of(remaining));
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

    private Product criarProduto(Long id, Store store) {
        Category category = new Category();
        category.setId(20L);
        category.setStore(store);

        Product product = new Product();
        product.setId(id);
        product.setName("Produto");
        product.setSlug("produto");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStore(store);
        product.setCategory(category);
        product.setInStock(true);
        product.setFeatured(false);
        product.setCreatedAt(LocalDateTime.now());
        return product;
    }

    private ProductImage criarImagem(Long id, Product product, String imageUrl, int position) {
        ProductImage image = new ProductImage();
        image.setId(id);
        image.setProduct(product);
        image.setImageUrl(imageUrl);
        image.setPosition(position);
        return image;
    }
}
