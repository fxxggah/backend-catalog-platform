package com.katallo.service;

import com.katallo.exception.BadRequestException;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CloudinaryService")
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @Test
    @DisplayName("Deve fazer upload de imagem válida")
    void deveFazerUploadDeImagemValida() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.jpg",
                "image/jpeg",
                "conteudo".getBytes()
        );

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://cloudinary.com/foto.jpg"));

        String url = cloudinaryService.uploadImage(file);

        assertThat(url).isEqualTo("https://cloudinary.com/foto.jpg");
    }

    @Test
    @DisplayName("Deve lançar erro quando imagem não for enviada")
    void deveLancarErroQuandoImagemNaoForEnviada() {
        assertThatThrownBy(() -> cloudinaryService.uploadImage(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Imagem não enviada.");
    }

    @Test
    @DisplayName("Deve lançar erro quando arquivo estiver vazio")
    void deveLancarErroQuandoArquivoEstiverVazio() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.jpg",
                "image/jpeg",
                new byte[]{}
        );

        assertThatThrownBy(() -> cloudinaryService.uploadImage(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Imagem não enviada.");
    }

    @Test
    @DisplayName("Deve lançar erro quando formato da imagem for inválido")
    void deveLancarErroQuandoFormatoDaImagemForInvalido() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "arquivo.pdf",
                "application/pdf",
                "conteudo".getBytes()
        );

        assertThatThrownBy(() -> cloudinaryService.uploadImage(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Formato de imagem inválido. Use JPG, PNG ou WEBP.");
    }

    @Test
    @DisplayName("Deve lançar erro quando Cloudinary não retornar URL segura")
    void deveLancarErroQuandoCloudinaryNaoRetornarUrlSegura() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.jpg",
                "image/jpeg",
                "conteudo".getBytes()
        );

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of());

        assertThatThrownBy(() -> cloudinaryService.uploadImage(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Erro ao obter URL da imagem enviada.");
    }
}
