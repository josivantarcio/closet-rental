package com.jtarcio.closetrental.modules.relatorios.infrastructure.adapter;

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
import com.jtarcio.closetrental.modules.relatorios.infrastructure.persistence.RelatorioRepositoryJpa;
import com.jtarcio.closetrental.modules.relatorios.infrastructure.persistence.projection.FaturamentoProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RelatorioRepositoryAdapter implements RelatorioRepositoryPort {

    private final RelatorioRepositoryJpa jpa;

    @Override
    public Page<DisponibilidadeDataResponse> disponibilidadeNaData(LocalDate data, Pageable pageable) {
        return jpa.findDisponiveisNaData(data, pageable);
    }

    @Override
    public Page<HistoricoClienteResponse> historicoCliente(Long clienteId, Pageable pageable) {
        return jpa.findHistoricoCliente(clienteId, pageable);
    }

    @Override
    public FaturamentoResponse faturamento(int mes, int ano) {
        FaturamentoProjection proj = jpa.findFaturamento(mes, ano);
        BigDecimal total = proj.getTotalReceita() != null ? proj.getTotalReceita() : BigDecimal.ZERO;
        long count = proj.getTotalPagamentos() != null ? proj.getTotalPagamentos() : 0L;
        return new FaturamentoResponse(mes, ano, total, count);
    }

    @Override
    public List<PecaMaisAlugadaResponse> pecasMaisAlugadas(int limite) {
        return jpa.findPecasMaisAlugadas(PageRequest.of(0, limite)).getContent();
    }

    @Override
    public Page<AtrasosPendentesResponse> atrasosPendentes(LocalDate hoje, Pageable pageable) {
        return jpa.findAtrasosPendentes(hoje, pageable);
    }

    @Override
    public List<TopClienteResponse> topClientes(int limite) {
        return jpa.findTopClientes(limite).stream().map(row -> new TopClienteResponse(
                toLong(row[0]),
                str(row[1]),
                toLong(row[2]),
                toBD(row[3])
        )).toList();
    }

    @Override
    public List<TicketMedioResponse> ticketMedioPorPeriodo(LocalDate de, LocalDate ate) {
        LocalDateTime inicio = de.atStartOfDay();
        LocalDateTime fim    = ate.plusDays(1).atStartOfDay();
        return jpa.findTicketMedioPorPeriodo(inicio, fim).stream().map(row -> {
            int mes            = toInt(row[0]);
            int ano            = toInt(row[1]);
            long totalLocacoes = toLong(row[2]);
            BigDecimal receita = toBD(row[3]);
            BigDecimal ticket  = totalLocacoes > 0
                    ? receita.divide(BigDecimal.valueOf(totalLocacoes), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            return new TicketMedioResponse(mes, ano, totalLocacoes, receita, ticket);
        }).toList();
    }

    @Override
    public List<ReceitaCategoriaResponse> receitaPorCategoria() {
        return jpa.findReceitaPorCategoria().stream().map(row -> new ReceitaCategoriaResponse(
                str(row[0]),
                toLong(row[1]),
                toBD(row[2])
        )).toList();
    }

    @Override
    public FaturamentoPeriodoResponse faturamentoPorPeriodo(LocalDate de, LocalDate ate) {
        LocalDateTime inicio = de.atStartOfDay();
        LocalDateTime fim    = ate.plusDays(1).atStartOfDay();
        List<FaturamentoResponse> meses = jpa.findFaturamentoPorPeriodo(inicio, fim).stream()
                .map(row -> new FaturamentoResponse(toInt(row[0]), toInt(row[1]), toBD(row[2]), toLong(row[3])))
                .toList();
        BigDecimal total   = meses.stream().map(FaturamentoResponse::totalReceita).reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalPagamentos = meses.stream().mapToLong(FaturamentoResponse::totalPagamentos).sum();
        return new FaturamentoPeriodoResponse(meses, total, totalPagamentos);
    }

    // ── helpers de conversão de Object[] ─────────────────────────────────────

    private String str(Object o)     { return o != null ? o.toString() : ""; }
    private BigDecimal toBD(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal bd) return bd;
        return new BigDecimal(o.toString());
    }
    private long toLong(Object o)    { return o == null ? 0L : ((Number) o).longValue(); }
    private int  toInt(Object o)     { return o == null ? 0  : ((Number) o).intValue(); }
}
