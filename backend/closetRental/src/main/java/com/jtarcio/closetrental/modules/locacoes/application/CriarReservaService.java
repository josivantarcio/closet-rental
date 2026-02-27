package com.jtarcio.closetrental.modules.locacoes.application;

import com.jtarcio.closetrental.modules.catalogo.domain.model.Produto;
import com.jtarcio.closetrental.modules.catalogo.domain.ports.ProdutoRepositoryPort;
import com.jtarcio.closetrental.modules.clientes.domain.ports.ClienteRepositoryPort;
import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.ConflictException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Use Case: Criar uma nova reserva.
 *
 * Regras aplicadas:
 * 1. dataInicio deve ser anterior a dataFim
 * 2. dataInicio nao pode ser no passado
 * 3. Cliente deve existir e estar ativo
 * 4. Produto deve existir e estar disponivel
 * 5. Nenhuma locacao ativa pode se sobrepor ao periodo para o mesmo produto (O(log n) com indice)
 * 6. Calcula valorTotal = valorDiaria * dias + caucao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CriarReservaService {

    private final LocacaoRepositoryPort locacaoPort;
    private final ProdutoRepositoryPort produtoPort;
    private final ClienteRepositoryPort clientePort;

    @Value("${app.locacao.caucao-padrao:100.00}")
    private BigDecimal caucaoPadrao;

    @Transactional
    public Locacao executar(Long clienteId, Long produtoId,
                            LocalDate dataInicio, LocalDate dataFim,
                            BigDecimal caucaoCustom, String observacoes) {

        // 1. Validar datas
        if (!dataInicio.isBefore(dataFim)) {
            throw new BusinessException("A data de inicio deve ser anterior a data de fim");
        }
        if (dataInicio.isBefore(LocalDate.now())) {
            throw new BusinessException("A data de inicio nao pode ser no passado");
        }

        // 2. Validar cliente
        var cliente = clientePort.buscarPorId(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente nao encontrado com id: " + clienteId));
        if (!cliente.isAtivo()) {
            throw new BusinessException("Nao e possivel criar reserva para cliente inativo");
        }

        // 3. Validar produto
        Produto produto = produtoPort.buscarPorId(produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Produto nao encontrado com id: " + produtoId));
        if (!produto.isDisponivel()) {
            throw new BusinessException("Produto nao esta disponivel para locacao");
        }

        // 4. Verificar overlap — O(log n) com indice composto
        boolean temOverlap = locacaoPort.existeOverlapNoPeriodo(produtoId, dataInicio, dataFim, null);
        if (temOverlap) {
            throw new ConflictException(
                "Produto ja possui locacao ativa no periodo de " + dataInicio + " a " + dataFim);
        }

        // 5. Calcular valores
        BigDecimal caucaoEfetiva = (caucaoCustom != null) ? caucaoCustom : caucaoPadrao;

        Locacao locacao = new Locacao();
        locacao.setClienteId(clienteId);
        locacao.setProdutoId(produtoId);
        locacao.setDataInicio(dataInicio);
        locacao.setDataFim(dataFim);
        locacao.setStatus(StatusLocacao.RESERVADO);
        locacao.setValorDiaria(produto.getValorDiaria());
        locacao.setCaucao(caucaoEfetiva);
        locacao.setMulta(BigDecimal.ZERO);
        locacao.setValorTotal(locacao.calcularValorTotal());
        locacao.setObservacoes(observacoes);

        Locacao salva = locacaoPort.salvar(locacao);
        log.info("Reserva criada: id={}, cliente={}, produto={}, periodo={} a {}, valorTotal={}",
                salva.getId(), clienteId, produtoId, dataInicio, dataFim, salva.getValorTotal());
        return salva;
    }
}
