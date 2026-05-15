package com.katallo.config;

import com.katallo.resolver.CurrentUserArgumentResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("Testes do WebConfig")
class WebConfigTest {

    @Test
    @DisplayName("Deve registrar CurrentUserArgumentResolver")
    void deveRegistrarCurrentUserArgumentResolver() {
        CurrentUserArgumentResolver resolver = mock(CurrentUserArgumentResolver.class);
        WebConfig webConfig = new WebConfig(resolver);
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        webConfig.addArgumentResolvers(resolvers);

        assertThat(resolvers).contains(resolver);
    }
}
