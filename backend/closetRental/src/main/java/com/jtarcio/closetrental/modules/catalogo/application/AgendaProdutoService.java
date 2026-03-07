package com.jtarcio.closetrental.modules.catalogo.application;

import com.jtarcio.closetrental.modules.catalogo.api.dto.PeriodoOcupadoResponse;
import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Retorna o calendário de ocupação de um produto para um determinado mês.
 * A query aproveita o índice composto (produto_id, data_inicio, data_fim) — O(log n).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgendaProdutoService {

    private final ProdutoService produtoService;
    private final LocacaoRepositoryPort locacaoRepository;

    @Transactional(readOnly = true)
    public List<PeriodoOcupadoResponse> buscarAgenda(Long produtoId, YearMonth mes) {
        // Valida que o produto existe (lança EntityNotFoundException se não)
        produtoService.buscarPorId(produtoId);

        LocalDate inicioMes = mes.atDay(1);
        LocalDate fimMes    = mes.atEndOfMonth();

        log.info("Buscando agenda do produto {} para o mês {}", produtoId, mes);

        List<Locacao> locacoes = locacaoRepository.buscarPeriodosOcupadosNoMes(produtoId, inicioMes, fimMes);

        return locacoes.stream()
                .map(l -> new PeriodoOcupadoResponse(
                        l.getId(),
                        l.getDataInicio(),
                        l.getDataFim(),
                        l.getStatus().name()))
                .toList();
    }
}
