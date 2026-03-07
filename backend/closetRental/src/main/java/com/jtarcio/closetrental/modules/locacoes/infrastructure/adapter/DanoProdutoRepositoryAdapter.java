package com.jtarcio.closetrental.modules.locacoes.infrastructure.adapter;

import com.jtarcio.closetrental.modules.locacoes.domain.model.DanoProduto;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.DanoProdutoRepositoryPort;
import com.jtarcio.closetrental.modules.locacoes.infrastructure.mapper.DanoProdutoMapper;
import com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence.DanoProdutoRepositoryJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DanoProdutoRepositoryAdapter implements DanoProdutoRepositoryPort {

    private final DanoProdutoRepositoryJpa jpa;
    private final DanoProdutoMapper mapper;

    @Override
    public DanoProduto salvar(DanoProduto dano) {
        return mapper.toDomain(jpa.save(mapper.toEntity(dano)));
    }

    @Override
    public List<DanoProduto> buscarPorLocacao(Long locacaoId) {
        return jpa.findByLocacaoId(locacaoId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
