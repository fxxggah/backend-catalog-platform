package com.catalog.repository;

import com.catalog.domain.entity.User;
import com.catalog.domain.enums.Provider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Testes do UserRepository")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Deve encontrar usuário pelo email")
    void deveEncontrarUsuarioPorEmail() {
        User user = criarUsuario("Gabriel", "gabriel@email.com", Provider.GOOGLE, true);
        userRepository.save(user);

        var result = userRepository.findByEmail("gabriel@email.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("gabriel@email.com");
    }

    @Test
    @DisplayName("Deve encontrar usuário ativo pelo email")
    void deveEncontrarUsuarioAtivoPorEmail() {
        User user = criarUsuario("Gabriel", "ativo@email.com", Provider.GOOGLE, true);
        userRepository.save(user);

        var result = userRepository.findByEmailAndActiveTrue("ativo@email.com");

        assertThat(result).isPresent();
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("Não deve encontrar usuário inativo pelo email quando buscar apenas ativos")
    void naoDeveEncontrarUsuarioInativoPorEmail() {
        User user = criarUsuario("Gabriel", "inativo@email.com", Provider.GOOGLE, false);
        userRepository.save(user);

        var result = userRepository.findByEmailAndActiveTrue("inativo@email.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve encontrar usuário pelo email e provider")
    void deveEncontrarUsuarioPorEmailEProvider() {
        User user = criarUsuario("Gabriel", "google@email.com", Provider.GOOGLE, true);
        userRepository.save(user);

        var result = userRepository.findByEmailAndProvider("google@email.com", Provider.GOOGLE);

        assertThat(result).isPresent();
        assertThat(result.get().getProvider()).isEqualTo(Provider.GOOGLE);
    }

    private User criarUsuario(String name, String email, Provider provider, boolean active) {
        return User.builder()
                .name(name)
                .email(email)
                .provider(provider)
                .active(active)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}