package com.catalog.controller.admin;

import com.catalog.dto.productimage.ProductImageReorderRequest;
import com.catalog.dto.productimage.ProductImageResponse;
import com.catalog.dto.productimage.UploadImageRequest;
import com.catalog.service.ProductImageService;
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
@DisplayName("Testes do AdminProductImageController")
class AdminProductImageControllerTest {

    @Mock
    private ProductImageService productImageService;

    @InjectMocks
    private AdminProductImageController adminProductImageController;

    @Test
    @DisplayName("Deve fazer upload de imagem")
    void deveFazerUploadDeImagem() {
        UploadImageRequest request = new UploadImageRequest();
        ProductImageResponse image = ProductImageResponse.builder().id(1L).imageUrl("imagem.jpg").position(1).build();

        when(productImageService.upload("minha-loja", request, 99L)).thenReturn(image);

        var response = adminProductImageController.upload("minha-loja", 10L, request, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isEqualTo(image);
        assertThat(request.getProductId()).isEqualTo(10L);
        verify(productImageService).upload("minha-loja", request, 99L);
    }

    @Test
    @DisplayName("Deve listar imagens do produto")
    void deveListarImagensDoProduto() {
        List<ProductImageResponse> images = List.of(ProductImageResponse.builder().id(1L).imageUrl("imagem.jpg").position(1).build());

        when(productImageService.getByProduct("minha-loja", 10L)).thenReturn(images);

        var response = adminProductImageController.getByProduct("minha-loja", 10L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(images);
        verify(productImageService).getByProduct("minha-loja", 10L);
    }

    @Test
    @DisplayName("Deve deletar imagem")
    void deveDeletarImagem() {
        var response = adminProductImageController.delete("minha-loja", 1L, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(productImageService).delete("minha-loja", 1L, 99L);
    }

    @Test
    @DisplayName("Deve reordenar imagens")
    void deveReordenarImagens() {
        ProductImageReorderRequest request = new ProductImageReorderRequest();
        request.setImageIds(List.of(3L, 1L, 2L));

        var response = adminProductImageController.reorder("minha-loja", 10L, request, 99L);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(productImageService).reorder("minha-loja", 10L, request, 99L);
    }
}
