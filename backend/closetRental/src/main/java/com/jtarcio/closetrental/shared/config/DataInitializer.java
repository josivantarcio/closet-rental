package com.jtarcio.closetrental.shared.config;

import com.jtarcio.closetrental.modules.tenants.application.TenantService;
import com.jtarcio.closetrental.modules.tenants.domain.model.Tenant;
import com.jtarcio.closetrental.modules.tenants.domain.ports.TenantRepositoryPort;
import com.jtarcio.closetrental.modules.usuarios.application.UsuarioService;
import com.jtarcio.closetrental.modules.usuarios.domain.model.Usuario;
import com.jtarcio.closetrental.modules.usuarios.domain.ports.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Inicializa tenant padrão e usuários padrão no banco quando estiver vazio.
 * Executado apenas nos perfis dev e test (não em produção).
 *
 * Tenant criado: Closet Rental Demo (slug: default, UUID fixo)
 * Usuários criados:
 * - admin / admin123        → ADMIN (vinculado ao tenant padrão)
 * - atendente / atendente123 → ATENDENTE (vinculado ao tenant padrão)
 * - cliente / cliente123    → CLIENTE (vinculado ao tenant padrão)
 * - superadmin / super123   → SUPER_ADMIN (sem tenant)
 */
@Slf4j
@Component
@Profile({"default", "dev", "test", "render"})
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    static final UUID DEFAULT_TENANT_ID = UUID.fromString("a0000000-0000-0000-0000-000000000001");

    private final UsuarioRepositoryPort repositoryPort;
    private final UsuarioService usuarioService;
    private final TenantRepositoryPort tenantRepositoryPort;

    @Override
    public void run(ApplicationArguments args) {
        Tenant tenant = garantirTenantPadrao();

        if (repositoryPort.existePorUsername("admin")) {
            log.info("DataInitializer — usuários já existem, seed ignorado.");
            return;
        }

        log.info("DataInitializer — criando usuários padrão...");

        criarUsuario("Administrador", "admin", "admin@closetrental.com", "admin123", "ADMIN", tenant.getId());
        criarUsuario("Atendente", "atendente", "atendente@closetrental.com", "atendente123", "ATENDENTE", tenant.getId());
        criarUsuario("Cliente Demo", "cliente", "cliente@closetrental.com", "cliente123", "CLIENTE", tenant.getId());
        criarUsuario("Super Admin", "superadmin", "superadmin@softhouse.com.br", "super123", "SUPER_ADMIN", null);

        log.info("DataInitializer — 4 usuários criados com sucesso.");
    }

    private Tenant garantirTenantPadrao() {
        return tenantRepositoryPort.buscarPorSlug("default").orElseGet(() -> {
            log.info("DataInitializer — criando tenant padrão...");
            Tenant t = new Tenant();
            t.setNome("Closet Rental Demo");
            t.setSlug("default");
            t.setPlano("FREE");
            t.setAtivo(true);
            return tenantRepositoryPort.salvar(t);
        });
    }

    private void criarUsuario(String nome, String username, String email, String senha, String role, UUID tenantId) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setUsername(username);
        u.setEmail(email);
        u.setRole(role);
        u.setTenantId(tenantId);
        usuarioService.criar(u, senha);
    }
}
