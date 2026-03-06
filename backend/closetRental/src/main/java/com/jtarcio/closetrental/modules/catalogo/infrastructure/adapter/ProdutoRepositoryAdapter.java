package com.jtarcio.closetrental.modules.catalogo.infrastructure.adapter;

import com.jtarcio.closetrental.modules.catalogo.domain.model.Produto;
import com.jtarcio.closetrental.modules.catalogo.domain.ports.ProdutoRepositoryPort;
import com.jtarcio.closetrental.modules.catalogo.infrastructure.mapper.ProdutoMapper;
import com.jtarcio.closetrental.modules.catalogo.infrastructure.persistence.ProdutoRepositoryJpa;
import com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence.LocacaoRepositoryJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProdutoRepositoryAdapter implements ProdutoRepositoryPort {

    private final ProdutoRepositoryJpa jpaRepository;
    private final LocacaoRepositoryJpa locacaoRepositoryJpa;
    private final ProdutoMapper mapper;

    @Override
    public Produto salvar(Produto produto) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(produto)));
    }

    @Override
    public Page<Produto> listarTodos(UUID tenantId, Pageable pageable) {
        if (tenantId == null) {
            return jpaRepository.findAll(pageable).map(mapper::toDomain);
        }
        return jpaRepository.findByTenantId(tenantId, pageable).map(mapper::toDomain);
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Produto> buscarDisponiveisNoPeriodo(UUID tenantId, LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {
        if (tenantId == null) {
            return jpaRepository.buscarDisponiveisNoPeriodo(dataInicio, dataFim, pageable).map(mapper::toDomain);
        }
        return jpaRepository.buscarDisponiveisNoPeriodoByTenant(tenantId, dataInicio, dataFim, pageable).map(mapper::toDomain);
    }

    @Override
    public void deletar(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existeEmLocacaoAtiva(Long id) {
        return locacaoRepositoryJpa.existeLocacaoAtivaParaProduto(id);
    }
}
