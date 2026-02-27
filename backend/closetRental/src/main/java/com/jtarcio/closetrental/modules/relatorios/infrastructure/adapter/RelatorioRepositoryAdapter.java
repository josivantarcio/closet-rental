package com.jtarcio.closetrental.modules.relatorios.infrastructure.adapter;

import com.jtarcio.closetrental.modules.relatorios.api.dto.AtrasosPendentesResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.DisponibilidadeDataResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.FaturamentoResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.HistoricoClienteResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.PecaMaisAlugadaResponse;
import com.jtarcio.closetrental.modules.relatorios.domain.ports.RelatorioRepositoryPort;
import com.jtarcio.closetrental.modules.relatorios.infrastructure.persistence.RelatorioRepositoryJpa;
import com.jtarcio.closetrental.modules.relatorios.infrastructure.persistence.projection.FaturamentoProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        return jpa.findPecasMaisAlugadas(limite);
    }

    @Override
    public Page<AtrasosPendentesResponse> atrasosPendentes(LocalDate hoje, Pageable pageable) {
        return jpa.findAtrasosPendentes(hoje, pageable);
    }
}
