package com.jtarcio.closetrental.modules.tenants.infrastructure.adapter;

import com.jtarcio.closetrental.modules.tenants.domain.model.Tenant;
import com.jtarcio.closetrental.modules.tenants.domain.ports.TenantRepositoryPort;
import com.jtarcio.closetrental.modules.tenants.infrastructure.mapper.TenantMapper;
import com.jtarcio.closetrental.modules.tenants.infrastructure.persistence.TenantRepositoryJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TenantRepositoryAdapter implements TenantRepositoryPort {

    private final TenantRepositoryJpa jpa;
    private final TenantMapper mapper;

    @Override
    public Tenant salvar(Tenant tenant) {
        return mapper.toDomain(jpa.save(mapper.toEntity(tenant)));
    }

    @Override
    public Optional<Tenant> buscarPorId(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Tenant> buscarPorSlug(String slug) {
        return jpa.findBySlug(slug).map(mapper::toDomain);
    }

    @Override
    public Page<Tenant> listarTodos(Pageable pageable) {
        return jpa.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public boolean existePorSlug(String slug) {
        return jpa.existsBySlug(slug);
    }
}
