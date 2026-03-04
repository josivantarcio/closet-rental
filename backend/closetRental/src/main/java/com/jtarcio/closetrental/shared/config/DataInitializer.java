package com.jtarcio.closetrental.shared.config;

import com.jtarcio.closetrental.modules.usuarios.application.UsuarioService;
import com.jtarcio.closetrental.modules.usuarios.domain.model.Usuario;
import com.jtarcio.closetrental.modules.usuarios.domain.ports.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Inicializa usuários padrão no banco quando a tabela estiver vazia.
 * Executado apenas nos perfis dev e test (não em produção).
 *
 * Usuários criados:
 * - admin / admin123        → ADMIN
 * - atendente / atendente123 → ATENDENTE
 * - cliente / cliente123    → CLIENTE
 */
@Slf4j
@Component
@Profile({"default", "dev", "test"})
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepositoryPort repositoryPort;
    private final UsuarioService usuarioService;

    @Override
    public void run(ApplicationArguments args) {
        if (repositoryPort.existePorUsername("admin")) {
            log.info("DataInitializer — usuários já existem, seed ignorado.");
            return;
        }

        log.info("DataInitializer — criando usuários padrão...");

        criarUsuario("Administrador", "admin", "admin@closetrental.com", "admin123", "ADMIN");
        criarUsuario("Atendente", "atendente", "atendente@closetrental.com", "atendente123", "ATENDENTE");
        criarUsuario("Cliente Demo", "cliente", "cliente@closetrental.com", "cliente123", "CLIENTE");

        log.info("DataInitializer — 3 usuários criados com sucesso.");
    }

    private void criarUsuario(String nome, String username, String email, String senha, String role) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setUsername(username);
        u.setEmail(email);
        u.setRole(role);
        usuarioService.criar(u, senha);
    }
}
