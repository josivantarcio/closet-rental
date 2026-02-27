package com.jtarcio.closetrental.modules.relatorios.api;

import com.jtarcio.closetrental.modules.relatorios.api.dto.AtrasosPendentesResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.DisponibilidadeDataResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.FaturamentoResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.HistoricoClienteResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.PecaMaisAlugadaResponse;
import com.jtarcio.closetrental.modules.relatorios.application.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller REST do módulo de Relatórios.
 * Endpoint base: /v1/relatorios
 * Todos os endpoints exigem ADMIN ou ATENDENTE.
 */
@RestController
@RequestMapping("/v1/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Consultas gerenciais: disponibilidade, histórico, faturamento, ranking e atrasos")
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/disponibilidade")
    @Operation(
            summary = "Peças disponíveis em uma data",
            description = "Lista produtos sem locação ativa (RESERVADO ou RETIRADO) em uma data específica. Paginado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de peças disponíveis"),
        @ApiResponse(responseCode = "400", description = "Data inválida")
    })
    public Page<DisponibilidadeDataResponse> disponibilidade(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return relatorioService.disponibilidadeNaData(data, pageable);
    }

    @GetMapping("/historico-cliente/{clienteId}")
    @Operation(
            summary = "Histórico de locações de um cliente",
            description = "Retorna todas as locações do cliente informado, ordenadas por data de início decrescente. Paginado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Histórico retornado"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public Page<HistoricoClienteResponse> historicoCliente(
            @PathVariable Long clienteId,
            @PageableDefault(size = 20) Pageable pageable) {
        return relatorioService.historicoCliente(clienteId, pageable);
    }

    @GetMapping("/faturamento")
    @Operation(
            summary = "Faturamento mensal",
            description = "Soma e contagem de pagamentos APROVADOS em um mês/ano.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Faturamento calculado"),
        @ApiResponse(responseCode = "400", description = "Mês ou ano inválido")
    })
    public FaturamentoResponse faturamento(
            @RequestParam int mes,
            @RequestParam int ano) {
        return relatorioService.faturamento(mes, ano);
    }

    @GetMapping("/pecas-mais-alugadas")
    @Operation(
            summary = "Ranking de peças mais alugadas",
            description = "Lista os produtos com maior número de locações (excluindo canceladas), em ordem decrescente.")
    @ApiResponse(responseCode = "200", description = "Ranking retornado")
    public List<PecaMaisAlugadaResponse> pecasMaisAlugadas(
            @RequestParam(defaultValue = "10") int limite) {
        return relatorioService.pecasMaisAlugadas(limite);
    }

    @GetMapping("/atrasos-pendentes")
    @Operation(
            summary = "Locações em atraso",
            description = "Lista locações com status RETIRADO cuja data de devolução já passou. Inclui dias de atraso e multa estimada. Paginado.")
    @ApiResponse(responseCode = "200", description = "Atrasos retornados")
    public Page<AtrasosPendentesResponse> atrasosPendentes(
            @PageableDefault(size = 20, sort = "dataFim") Pageable pageable) {
        return relatorioService.atrasosPendentes(pageable);
    }
}
