package com.katallo.exception;

import com.katallo.domain.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Testes do GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Deve tratar ApiException retornando status e código corretos")
    void deveTratarApiExceptionRetornandoStatusECodigoCorretos() {
        HttpServletRequest request = request("/api/v1/stores/minha-loja");
        NotFoundException exception = new NotFoundException(ErrorCode.STORE_NOT_FOUND, "Loja não encontrada.");

        var response = handler.handleApiException(exception, request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("STORE_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("Loja não encontrada.");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/stores/minha-loja");
    }

    @Test
    @DisplayName("Deve tratar violação de constraint")
    void deveTratarViolacaoDeConstraint() {
        HttpServletRequest request = request("/api/v1/teste");
        ConstraintViolationException exception = new ConstraintViolationException("valor inválido", null);

        var response = handler.handleConstraintViolation(exception, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/teste");
    }

    @Test
    @DisplayName("Deve tratar upload acima do limite")
    void deveTratarUploadAcimaDoLimite() {
        HttpServletRequest request = request("/api/v1/admin/stores/minha-loja/products/1/images");
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(10L);

        var response = handler.handleMaxUploadSize(exception, request);

        assertThat(response.getStatusCode().value()).isEqualTo(413);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("UPLOAD_FAILED");
        assertThat(response.getBody().getMessage()).isEqualTo("Arquivo muito grande. Envie uma imagem menor.");
    }

    @Test
    @DisplayName("Deve tratar exceção genérica")
    void deveTratarExcecaoGenerica() {
        HttpServletRequest request = request("/api/v1/teste");

        var response = handler.handleGenericException(new RuntimeException("erro inesperado"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Erro interno no servidor.");
    }

    private HttpServletRequest request(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }
}
