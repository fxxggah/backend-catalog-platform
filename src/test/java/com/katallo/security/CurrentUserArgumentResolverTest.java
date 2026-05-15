package com.katallo.security;

import com.katallo.annotation.CurrentUser;
import com.katallo.resolver.CurrentUserArgumentResolver;
import com.katallo.support.SecurityTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do CurrentUserArgumentResolver")
class CurrentUserArgumentResolverTest {

    private final CurrentUserArgumentResolver resolver = new CurrentUserArgumentResolver();

    @AfterEach
    void limparContexto() {
        SecurityTestUtils.clearAuthentication();
    }

    @Test
    @DisplayName("Deve suportar parâmetro Long anotado com CurrentUser")
    void deveSuportarParametroLongAnotadoComCurrentUser() throws NoSuchMethodException {
        Method method = ControllerFake.class.getDeclaredMethod("metodoComCurrentUser", Long.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        assertThat(resolver.supportsParameter(parameter)).isTrue();
    }

    @Test
    @DisplayName("Deve resolver o id do usuário autenticado")
    void deveResolverIdDoUsuarioAutenticado() throws Exception {
        SecurityTestUtils.authenticateAs(99L);

        Method method = ControllerFake.class.getDeclaredMethod("metodoComCurrentUser", Long.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        Object result = resolver.resolveArgument(parameter, null, null, null);

        assertThat(result).isEqualTo(99L);
    }

    static class ControllerFake {
        void metodoComCurrentUser(@CurrentUser Long userId) {
        }
    }
}
