package com.jtarcio.closetrental.modules.relatorios.domain.ports;

import com.jtarcio.closetrental.modules.relatorios.api.dto.AtrasosPendentesResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.DisponibilidadeDataResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.FaturamentoPeriodoResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.FaturamentoResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.HistoricoClienteResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.PecaMaisAlugadaResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.ReceitaCategoriaResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.TicketMedioResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.TopClienteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface RelatorioRepositoryPort {

    Page<DisponibilidadeDataResponse> disponibilidadeNaData(LocalDate data, Pageable pageable);

    Page<HistoricoClienteResponse> historicoCliente(Long clienteId, Pageable pageable);

    FaturamentoResponse faturamento(int mes, int ano);

    List<PecaMaisAlugadaResponse> pecasMaisAlugadas(int limite);

    Page<AtrasosPendentesResponse> atrasosPendentes(LocalDate hoje, Pageable pageable);

    List<TopClienteResponse> topClientes(int limite);

    List<TicketMedioResponse> ticketMedioPorPeriodo(LocalDate de, LocalDate ate);

    List<ReceitaCategoriaResponse> receitaPorCategoria();

    FaturamentoPeriodoResponse faturamentoPorPeriodo(LocalDate de, LocalDate ate);
}
