package com.jtarcio.closetrental.modules.locacoes.domain.ports;

import com.jtarcio.closetrental.modules.locacoes.domain.model.DanoProduto;

import java.util.List;

public interface DanoProdutoRepositoryPort {

    DanoProduto salvar(DanoProduto dano);

    List<DanoProduto> buscarPorLocacao(Long locacaoId);
}
