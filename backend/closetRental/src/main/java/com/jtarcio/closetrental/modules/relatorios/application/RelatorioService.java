package com.jtarcio.closetrental.modules.relatorios.application;

import com.jtarcio.closetrental.modules.relatorios.api.dto.AtrasosPendentesResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.DisponibilidadeDataResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.FaturamentoPeriodoResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.FaturamentoResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.HistoricoClienteResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.PecaMaisAlugadaResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.ReceitaCategoriaResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.TicketMedioResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.TopClienteResponse;
import com.jtarcio.closetrental.modules.relatorios.domain.ports.RelatorioRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RelatorioService {

    private final RelatorioRepositoryPort relatorioPort;

    @Value("${app.locacao.percentual-multa-diaria:0.10}")
    private BigDecimal percentualMultaDiaria;

    public Page<DisponibilidadeDataResponse> disponibilidadeNaData(LocalDate data, Pageable pageable) {
        log.info("Relatório: disponibilidade na data {}", data);
        return relatorioPort.disponibilidadeNaData(data, pageable);
    }

    public Page<HistoricoClienteResponse> historicoCliente(Long clienteId, Pageable pageable) {
        log.info("Relatório: histórico do cliente {}", clienteId);
        return relatorioPort.historicoCliente(clienteId, pageable);
    }

    public FaturamentoResponse faturamento(int mes, int ano) {
        if (mes < 1 || mes > 12) {
            throw new BusinessException("Mês inválido: deve ser entre 1 e 12");
        }
        if (ano < 2000 || ano > 2100) {
            throw new BusinessException("Ano inválido: deve ser entre 2000 e 2100");
        }
        log.info("Relatório: faturamento {}/{}", mes, ano);
        return relatorioPort.faturamento(mes, ano);
    }

    public List<PecaMaisAlugadaResponse> pecasMaisAlugadas(int limite) {
        if (limite < 1 || limite > 100) {
            throw new BusinessException("Limite inválido: deve ser entre 1 e 100");
        }
        log.info("Relatório: peças mais alugadas (top {})", limite);
        return relatorioPort.pecasMaisAlugadas(limite);
    }

    public Page<AtrasosPendentesResponse> atrasosPendentes(Pageable pageable) {
        LocalDate hoje = LocalDate.now();
        log.info("Relatório: atrasos pendentes em {}", hoje);
        Page<AtrasosPendentesResponse> page = relatorioPort.atrasosPendentes(hoje, pageable);
        return page.map(r -> AtrasosPendentesResponse.of(
                r.locacaoId(), r.clienteId(), r.produtoId(),
                r.dataFim(), hoje, r.valorDiaria(), percentualMultaDiaria));
    }

    public List<TopClienteResponse> topClientes(int limite) {
        if (limite < 1 || limite > 100) throw new BusinessException("Limite inválido: 1–100");
        log.info("Relatório: top {} clientes", limite);
        return relatorioPort.topClientes(limite);
    }

    public List<TicketMedioResponse> ticketMedio(LocalDate de, LocalDate ate) {
        validarPeriodo(de, ate);
        log.info("Relatório: ticket médio {} a {}", de, ate);
        return relatorioPort.ticketMedioPorPeriodo(de, ate);
    }

    public List<ReceitaCategoriaResponse> receitaPorCategoria() {
        log.info("Relatório: receita por categoria");
        return relatorioPort.receitaPorCategoria();
    }

    public FaturamentoPeriodoResponse faturamentoPorPeriodo(LocalDate de, LocalDate ate) {
        validarPeriodo(de, ate);
        log.info("Relatório: faturamento por período {} a {}", de, ate);
        return relatorioPort.faturamentoPorPeriodo(de, ate);
    }

    private void validarPeriodo(LocalDate de, LocalDate ate) {
        if (de == null || ate == null) throw new BusinessException("Datas de início e fim são obrigatórias");
        if (de.isAfter(ate)) throw new BusinessException("Data de início deve ser anterior à data de fim");
    }
}
