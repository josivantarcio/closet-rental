package com.jtarcio.closetrental.modules.locacoes.infrastructure.adapter;

import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.modules.locacoes.infrastructure.mapper.LocacaoMapper;
import com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence.ItemLocacaoRepositoryJpa;
import com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence.LocacaoRepositoryJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter que implementa LocacaoRepositoryPort usando JPA + MapStruct.
 */
@Component
@RequiredArgsConstructor
public class LocacaoRepositoryAdapter implements LocacaoRepositoryPort {

    private final LocacaoRepositoryJpa jpa;
    private final ItemLocacaoRepositoryJpa itemJpa;
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
        return itemJpa.existeOverlapNoPeriodo(produtoId, dataInicio, dataFim, excluirLocacaoId);
    }

    @Override
    public boolean existeLocacaoAtivaParaProduto(Long produtoId) {
        return itemJpa.existeLocacaoAtivaParaProduto(produtoId);
    }

    @Override
    public boolean existeLocacaoAtivaParaCliente(Long clienteId) {
        return jpa.existeLocacaoAtivaParaCliente(clienteId);
    }

    @Override
    public List<Locacao> buscarPeriodosOcupadosNoMes(Long produtoId, LocalDate inicioMes, LocalDate fimMes) {
        return jpa.buscarPeriodosOcupadosNoMes(produtoId, inicioMes, fimMes)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Locacao> buscarReservadosComRetiradaEm(LocalDate data) {
        return jpa.buscarReservadosComRetiradaEm(data).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Locacao> buscarRetiradosComDevolucaoEm(LocalDate data) {
        return jpa.buscarRetiradosComDevolucaoEm(data).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Locacao> buscarRetiradosEmAtraso(LocalDate hoje) {
        return jpa.buscarRetiradosEmAtraso(hoje).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Long> listarIdsPorCliente(UUID tenantId, Long clienteId) {
        return jpa.findIdsByTenantIdAndClienteId(tenantId, clienteId);
    }
}
