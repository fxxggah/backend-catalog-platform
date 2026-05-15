package com.katallo.support;

import org.mockito.Mockito;

public final class MockFactory {

    private MockFactory() {
    }

    public static <T> T mock(Class<T> type) {
        return Mockito.mock(type);
    }
}
