package com.katallo.repository;

import com.katallo.domain.entity.User;
import com.katallo.domain.enums.Provider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Testes do UserRepository")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Deve encontrar usuário pelo e-mail")
    void deveEncontrarUsuarioPorEmail() {
        User user = criarUsuario("Gabriel", "gabriel@email.com", true);
        userRepository.save(user);

        var result = userRepository.findByEmail("gabriel@email.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("gabriel@email.com");
    }

    @Test
    @DisplayName("Deve encontrar usuário ativo pelo e-mail")
    void deveEncontrarUsuarioAtivoPorEmail() {
        User user = criarUsuario("Gabriel", "ativo@email.com", true);
        userRepository.save(user);

        var result = userRepository.findByEmailAndActiveTrue("ativo@email.com");

        assertThat(result).isPresent();
        assertThat(result.get().getActive()).isTrue();
    }

    @Test
    @DisplayName("Não deve encontrar usuário inativo ao buscar apenas ativos")
    void naoDeveEncontrarUsuarioInativoAoBuscarApenasAtivos() {
        User user = criarUsuario("Gabriel", "inativo@email.com", false);
        userRepository.save(user);

        var result = userRepository.findByEmailAndActiveTrue("inativo@email.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve encontrar usuário pelo e-mail e provider")
    void deveEncontrarUsuarioPorEmailEProvider() {
        User user = criarUsuario("Gabriel", "google@email.com", true);
        userRepository.save(user);

        var result = userRepository.findByEmailAndProvider("google@email.com", Provider.GOOGLE);

        assertThat(result).isPresent();
        assertThat(result.get().getProvider()).isEqualTo(Provider.GOOGLE);
    }

    private User criarUsuario(String name, String email, boolean active) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setProvider(Provider.GOOGLE);
        user.setActive(active);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}