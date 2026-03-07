package com.jtarcio.closetrental.modules.relatorios.api;

import com.jtarcio.closetrental.modules.relatorios.api.dto.AtrasosPendentesResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.DisponibilidadeDataResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.FaturamentoPeriodoResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.FaturamentoResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.HistoricoClienteResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.PecaMaisAlugadaResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.ReceitaCategoriaResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.TicketMedioResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.TopClienteResponse;
import com.jtarcio.closetrental.modules.relatorios.application.ExportacaoService;
import com.jtarcio.closetrental.modules.relatorios.application.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
 */
@RestController
@RequestMapping("/v1/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Consultas gerenciais: faturamento, gráficos, ranking, exportação CSV/PDF")
public class RelatorioController {

    private final RelatorioService relatorioService;
    private final ExportacaoService exportacaoService;

    // ── Endpoints existentes ─────────────────────────────────────────────────

    @GetMapping("/disponibilidade")
    @Operation(summary = "Peças disponíveis em uma data")
    public Page<DisponibilidadeDataResponse> disponibilidade(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return relatorioService.disponibilidadeNaData(data, pageable);
    }

    @GetMapping("/historico-cliente/{clienteId}")
    @Operation(summary = "Histórico de locações de um cliente")
    public Page<HistoricoClienteResponse> historicoCliente(
            @PathVariable Long clienteId,
            @PageableDefault(size = 20) Pageable pageable) {
        return relatorioService.historicoCliente(clienteId, pageable);
    }

    @GetMapping("/faturamento")
    @Operation(summary = "Faturamento mensal (mês/ano)")
    public FaturamentoResponse faturamento(
            @RequestParam int mes,
            @RequestParam int ano) {
        return relatorioService.faturamento(mes, ano);
    }

    @GetMapping("/pecas-mais-alugadas")
    @Operation(summary = "Ranking de peças mais alugadas")
    public List<PecaMaisAlugadaResponse> pecasMaisAlugadas(
            @RequestParam(defaultValue = "10") int limite) {
        return relatorioService.pecasMaisAlugadas(limite);
    }

    @GetMapping("/atrasos-pendentes")
    @Operation(summary = "Locações em atraso")
    public Page<AtrasosPendentesResponse> atrasosPendentes(
            @PageableDefault(size = 20, sort = "dataFim") Pageable pageable) {
        return relatorioService.atrasosPendentes(pageable);
    }

    // ── Novos endpoints Fase 5 ────────────────────────────────────────────────

    @GetMapping("/faturamento/periodo")
    @Operation(summary = "Faturamento mensal num intervalo de datas (para gráfico de linha)")
    public FaturamentoPeriodoResponse faturamentoPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return relatorioService.faturamentoPorPeriodo(de, ate);
    }

    @GetMapping("/top-clientes")
    @Operation(summary = "Ranking de clientes por receita gerada")
    public List<TopClienteResponse> topClientes(
            @RequestParam(defaultValue = "10") int limite) {
        return relatorioService.topClientes(limite);
    }

    @GetMapping("/ticket-medio")
    @Operation(summary = "Ticket médio por mês num intervalo de datas")
    public List<TicketMedioResponse> ticketMedio(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return relatorioService.ticketMedio(de, ate);
    }

    @GetMapping("/receita-por-categoria")
    @Operation(summary = "Receita total por categoria de produto (para gráfico de pizza)")
    public List<ReceitaCategoriaResponse> receitaPorCategoria() {
        return relatorioService.receitaPorCategoria();
    }

    // ── Exportação ────────────────────────────────────────────────────────────

    @GetMapping("/exportar")
    @Operation(summary = "Exporta relatório em CSV ou PDF",
               description = "tipo: faturamento | pecas-mais-alugadas | top-clientes | formato: CSV | PDF")
    public ResponseEntity<byte[]> exportar(
            @RequestParam String tipo,
            @RequestParam(defaultValue = "CSV") String formato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {

        LocalDate dataInicio = de  != null ? de  : LocalDate.now().minusMonths(12);
        LocalDate dataFim    = ate != null ? ate : LocalDate.now();

        byte[] dados;
        String contentType;
        String nomeArquivo;

        if ("PDF".equalsIgnoreCase(formato)) {
            dados = exportacaoService.exportarPdf(tipo, dataInicio, dataFim);
            contentType = "application/pdf";
            nomeArquivo = tipo + ".pdf";
        } else {
            dados = exportacaoService.exportarCsv(tipo, dataInicio, dataFim);
            contentType = "text/csv;charset=UTF-8";
            nomeArquivo = tipo + ".csv";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDisposition(ContentDisposition.attachment().filename(nomeArquivo).build());
        return ResponseEntity.ok().headers(headers).body(dados);
    }
}
