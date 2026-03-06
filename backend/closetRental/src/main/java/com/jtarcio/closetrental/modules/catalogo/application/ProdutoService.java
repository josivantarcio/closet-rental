package com.jtarcio.closetrental.modules.catalogo.application;

import com.jtarcio.closetrental.modules.catalogo.domain.model.Produto;
import com.jtarcio.closetrental.modules.catalogo.domain.ports.ProdutoRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import com.jtarcio.closetrental.shared.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepositoryPort repositoryPort;

    @Transactional
    public Produto criar(Produto produto) {
        validarValorDiaria(produto.getValorDiaria());
        produto.setTenantId(TenantContext.getTenantId());
        log.info("Criando produto: {}", produto.getNome());
        return repositoryPort.salvar(produto);
    }

    @Transactional(readOnly = true)
    public Page<Produto> listar(Pageable pageable) {
        log.info("Listando produtos");
        return repositoryPort.listarTodos(TenantContext.getTenantId(), pageable);
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        log.info("Buscando produto id: {}", id);
        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com id: " + id));
    }

    @Transactional
    public Produto atualizar(Long id, Produto produto) {
        Produto existente = buscarPorId(id);
        validarValorDiaria(produto.getValorDiaria());
        existente.setNome(produto.getNome());
        existente.setCategoria(produto.getCategoria());
        existente.setValorDiaria(produto.getValorDiaria());
        existente.setCodigo(produto.getCodigo());
        existente.setDisponivel(produto.isDisponivel());
        existente.setTamanho(produto.getTamanho());
        existente.setCor(produto.getCor());
        existente.setDescricao(produto.getDescricao());
        existente.setImagemUrl(produto.getImagemUrl());
        log.info("Atualizando produto id: {}", id);
        return repositoryPort.salvar(existente);
    }

    @Transactional
    public void deletar(Long id) {
        buscarPorId(id);
        if (repositoryPort.existeEmLocacaoAtiva(id)) {
            throw new BusinessException("Não é possível deletar um produto em locação ativa");
        }
        log.info("Deletando produto id: {}", id);
        repositoryPort.deletar(id);
    }

    private void validarValorDiaria(BigDecimal valorDiaria) {
        if (valorDiaria != null && valorDiaria.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("O valor da diária não pode ser negativo");
        }
    }
}
