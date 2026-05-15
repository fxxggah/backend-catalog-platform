package com.katallo.support;

import org.junit.jupiter.api.AfterEach;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityTestUtils.clearAuthentication();
    }
}
