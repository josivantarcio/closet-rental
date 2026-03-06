package com.jtarcio.closetrental.modules.tenants.domain.ports;

import com.jtarcio.closetrental.modules.tenants.domain.model.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepositoryPort {

    Tenant salvar(Tenant tenant);

    Optional<Tenant> buscarPorId(UUID id);

    Optional<Tenant> buscarPorSlug(String slug);

    Page<Tenant> listarTodos(Pageable pageable);

    boolean existePorSlug(String slug);
}
