package com.katallo.support;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

public final class SecurityTestUtils {

    private SecurityTestUtils() {
    }

    public static void authenticateAs(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList())
        );
    }

    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}
