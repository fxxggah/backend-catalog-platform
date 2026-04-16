package com.catalog.auth;

import com.catalog.dto.auth.GoogleUserData;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleTokenVerifier {

    private final String CLIENT_ID = "SEU_GOOGLE_CLIENT_ID";

    public GoogleUserData verify(String token) {

        try {

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(token);

            if (idToken == null) {
                throw new RuntimeException("Token inválido");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            return new GoogleUserData(
                    payload.getSubject(), // googleId
                    payload.getEmail(),
                    (String) payload.get("name")
            );

        } catch (Exception e) {
            throw new RuntimeException("Erro ao validar token do Google");
        }
    }
}