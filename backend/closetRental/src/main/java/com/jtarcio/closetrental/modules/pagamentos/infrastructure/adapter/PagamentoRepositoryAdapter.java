package com.jtarcio.closetrental.modules.pagamentos.infrastructure.adapter;

import com.jtarcio.closetrental.modules.pagamentos.domain.model.Pagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.StatusPagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.ports.PagamentoRepositoryPort;
import com.jtarcio.closetrental.modules.pagamentos.infrastructure.mapper.PagamentoMapper;
import com.jtarcio.closetrental.modules.pagamentos.infrastructure.persistence.PagamentoRepositoryJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter que implementa PagamentoRepositoryPort usando JPA + MapStruct.
 */
@Component
@RequiredArgsConstructor
public class PagamentoRepositoryAdapter implements PagamentoRepositoryPort {

    private final PagamentoRepositoryJpa jpa;
    private final PagamentoMapper mapper;

    @Override
    public Pagamento salvar(Pagamento pagamento) {
        return mapper.toDomain(jpa.save(mapper.toEntity(pagamento)));
    }

    @Override
    public Optional<Pagamento> buscarPorId(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Pagamento> listarPorLocacao(Long locacaoId, Pageable pageable) {
        return jpa.findByLocacaoIdOrderByCriadoEmDesc(locacaoId, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Pagamento> listarPorStatus(UUID tenantId, StatusPagamento status, Pageable pageable) {
        if (tenantId == null) {
            return jpa.findByStatusOrderByCriadoEmDesc(status, pageable).map(mapper::toDomain);
        }
        return jpa.findByTenantIdAndStatusOrderByCriadoEmDesc(tenantId, status, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Pagamento> listarTodos(UUID tenantId, Pageable pageable) {
        if (tenantId == null) {
            return jpa.findAll(pageable).map(mapper::toDomain);
        }
        return jpa.findByTenantIdOrderByCriadoEmDesc(tenantId, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Pagamento> listarPorLocacoes(Collection<Long> locacaoIds, Pageable pageable) {
        if (locacaoIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return jpa.findByLocacaoIdInOrderByCriadoEmDesc(locacaoIds, pageable).map(mapper::toDomain);
    }
}
