package com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DanoProdutoRepositoryJpa extends JpaRepository<DanoProdutoEntity, Long> {

    List<DanoProdutoEntity> findByLocacaoId(Long locacaoId);
}
