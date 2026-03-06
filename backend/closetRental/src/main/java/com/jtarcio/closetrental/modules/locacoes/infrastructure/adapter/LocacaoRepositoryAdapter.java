package com.jtarcio.closetrental.modules.locacoes.infrastructure.adapter;

import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.modules.locacoes.infrastructure.mapper.LocacaoMapper;
import com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence.LocacaoRepositoryJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter que implementa LocacaoRepositoryPort usando JPA + MapStruct.
 */
@Component
@RequiredArgsConstructor
public class LocacaoRepositoryAdapter implements LocacaoRepositoryPort {

    private final LocacaoRepositoryJpa jpa;
    private final LocacaoMapper mapper;

    @Override
    public Locacao salvar(Locacao locacao) {
        return mapper.toDomain(jpa.save(mapper.toEntity(locacao)));
    }

    @Override
    public Optional<Locacao> buscarPorId(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Locacao> listarPorCliente(UUID tenantId, Long clienteId, Pageable pageable) {
        if (tenantId == null) {
            return jpa.findByClienteId(clienteId, pageable).map(mapper::toDomain);
        }
        return jpa.findByTenantIdAndClienteId(tenantId, clienteId, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Locacao> listarPorStatus(UUID tenantId, StatusLocacao status, Pageable pageable) {
        if (tenantId == null) {
            return jpa.findByStatus(status, pageable).map(mapper::toDomain);
        }
        return jpa.findByTenantIdAndStatus(tenantId, status, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Locacao> listarTodos(UUID tenantId, Pageable pageable) {
        if (tenantId == null) {
            return jpa.findAll(pageable).map(mapper::toDomain);
        }
        return jpa.findByTenantId(tenantId, pageable).map(mapper::toDomain);
    }

    @Override
    public boolean existeOverlapNoPeriodo(Long produtoId, LocalDate dataInicio, LocalDate dataFim, Long excluirLocacaoId) {
        return jpa.existeOverlapNoPeriodo(produtoId, dataInicio, dataFim, excluirLocacaoId);
    }

    @Override
    public boolean existeLocacaoAtivaParaProduto(Long produtoId) {
        return jpa.existeLocacaoAtivaParaProduto(produtoId);
    }

    @Override
    public boolean existeLocacaoAtivaParaCliente(Long clienteId) {
        return jpa.existeLocacaoAtivaParaCliente(clienteId);
    }
}
