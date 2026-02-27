package com.jtarcio.closetrental.modules.estoque.api;

import com.jtarcio.closetrental.modules.estoque.api.dto.*;
import com.jtarcio.closetrental.modules.estoque.application.EstoqueService;
import com.jtarcio.closetrental.modules.estoque.domain.model.TipoMovimentacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST do modulo de Estoque.
 * Endpoint base: /v1/estoque
 */
@RestController
@RequestMapping("/v1/estoque")
@RequiredArgsConstructor
@Tag(name = "Estoque", description = "Gestao de movimentacoes e disponibilidade de pecas")
public class EstoqueController {

    private final EstoqueService estoqueService;

    @GetMapping("/disponibilidade/{produtoId}")
    @Operation(summary = "Verificar disponibilidade",
               description = "Retorna o status de disponibilidade de uma peca e o total de locacoes realizadas.")
    @ApiResponse(responseCode = "200", description = "Disponibilidade obtida")
    @ApiResponse(responseCode = "404", description = "Produto nao encontrado")
    public DisponibilidadeResponse verificarDisponibilidade(@PathVariable Long produtoId) {
        return DisponibilidadeResponse.fromService(estoqueService.verificarDisponibilidade(produtoId));
    }

    @PostMapping("/manutencao")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enviar peca para manutencao",
               description = "Marca a peca como indisponivel e registra a movimentacao de manutencao.")
    @ApiResponse(responseCode = "201", description = "Peca enviada para manutencao")
    @ApiResponse(responseCode = "400", description = "Peca ja indisponivel")
    @ApiResponse(responseCode = "404", description = "Produto nao encontrado")
    public MovimentacaoResponse enviarParaManutencao(
            @Valid @RequestBody ManutencaoRequest request,
            Authentication auth) {
        String operador = auth != null ? auth.getName() : "sistema";
        return MovimentacaoResponse.fromDomain(
                estoqueService.enviarParaManutencao(request.produtoId(), request.motivo(), operador));
    }

    @PostMapping("/manutencao/retorno")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar retorno da manutencao",
               description = "Marca a peca como disponivel apos a conclusao da manutencao.")
    @ApiResponse(responseCode = "201", description = "Retorno registrado")
    @ApiResponse(responseCode = "400", description = "Peca ja esta disponivel")
    @ApiResponse(responseCode = "404", description = "Produto nao encontrado")
    public MovimentacaoResponse retornarDaManutencao(
            @Valid @RequestBody RetornoManutencaoRequest request,
            Authentication auth) {
        String operador = auth != null ? auth.getName() : "sistema";
        return MovimentacaoResponse.fromDomain(
                estoqueService.retornarDaManutencao(request.produtoId(), request.observacoes(), operador));
    }

    @GetMapping("/historico/{produtoId}")
    @Operation(summary = "Historico de movimentacoes de uma peca",
               description = "Lista todas as movimentacoes de uma peca, ordenadas da mais recente para a mais antiga.")
    public Page<MovimentacaoResponse> historicoPorProduto(
            @PathVariable Long produtoId,
            @PageableDefault(size = 20) Pageable pageable) {
        return estoqueService.listarHistoricoPorProduto(produtoId, pageable)
                .map(MovimentacaoResponse::fromDomain);
    }

    @GetMapping("/historico")
    @Operation(summary = "Historico completo de movimentacoes",
               description = "Lista todas as movimentacoes do estoque. Filtre por tipo via query param.")
    public Page<MovimentacaoResponse> historico(
            @RequestParam(required = false) TipoMovimentacao tipo,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        if (tipo != null) {
            return estoqueService.listarPorTipo(tipo, pageable).map(MovimentacaoResponse::fromDomain);
        }
        return estoqueService.listarTodos(pageable).map(MovimentacaoResponse::fromDomain);
    }
}
