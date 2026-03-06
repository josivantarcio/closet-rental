package com.jtarcio.closetrental.modules.tenants.application;

import com.jtarcio.closetrental.modules.tenants.domain.model.Tenant;
import com.jtarcio.closetrental.modules.tenants.domain.ports.TenantRepositoryPort;
import com.jtarcio.closetrental.shared.exception.ConflictException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepositoryPort repositoryPort;

    @Transactional
    public Tenant criar(Tenant tenant) {
        if (repositoryPort.existePorSlug(tenant.getSlug())) {
            throw new ConflictException("Já existe um tenant com o slug: " + tenant.getSlug());
        }
        tenant.setAtivo(true);
        log.info("Criando tenant: slug={}", tenant.getSlug());
        return repositoryPort.salvar(tenant);
    }

    @Transactional(readOnly = true)
    public Page<Tenant> listar(Pageable pageable) {
        return repositoryPort.listarTodos(pageable);
    }

    @Transactional(readOnly = true)
    public Tenant buscarPorId(UUID id) {
        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntityNotFoundException("Tenant não encontrado: " + id));
    }

    @Transactional
    public Tenant ativar(UUID id) {
        Tenant tenant = buscarPorId(id);
        tenant.ativar();
        log.info("Ativando tenant id={}", id);
        return repositoryPort.salvar(tenant);
    }

    @Transactional
    public Tenant desativar(UUID id) {
        Tenant tenant = buscarPorId(id);
        tenant.desativar();
        log.info("Desativando tenant id={}", id);
        return repositoryPort.salvar(tenant);
    }
}
