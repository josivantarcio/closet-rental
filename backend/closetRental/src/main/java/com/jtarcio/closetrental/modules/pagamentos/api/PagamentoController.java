package com.jtarcio.closetrental.modules.pagamentos.api;

import com.jtarcio.closetrental.modules.pagamentos.api.dto.EstornoRequest;
import com.jtarcio.closetrental.modules.pagamentos.api.dto.PagamentoRequest;
import com.jtarcio.closetrental.modules.pagamentos.api.dto.PagamentoResponse;
import com.jtarcio.closetrental.modules.pagamentos.application.EstornarPagamentoService;
import com.jtarcio.closetrental.modules.pagamentos.application.PagamentoQueryService;
import com.jtarcio.closetrental.modules.pagamentos.application.ProcessarPagamentoService;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.StatusPagamento;
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
 * Controller REST do módulo de Pagamentos.
 * Endpoint base: /v1/pagamentos
 */
@RestController
@RequestMapping("/v1/pagamentos")
@RequiredArgsConstructor
@Tag(name = "Pagamentos", description = "Processamento e consulta de pagamentos de locações")
public class PagamentoController {

    private final ProcessarPagamentoService processarService;
    private final EstornarPagamentoService estornarService;
    private final PagamentoQueryService queryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Processar pagamento",
               description = "Processa o pagamento de uma locação via gateway mock. " +
                             "A locação deve estar em status RETIRADO ou DEVOLVIDO.")
    @ApiResponse(responseCode = "201", description = "Pagamento processado (APROVADO ou FALHA)")
    @ApiResponse(responseCode = "400", description = "Dados inválidos ou regra de negócio violada")
    @ApiResponse(responseCode = "404", description = "Locação não encontrada")
    public PagamentoResponse processar(
            @Valid @RequestBody PagamentoRequest request,
            Authentication auth) {
        String operador = auth != null ? auth.getName() : "sistema";
        return PagamentoResponse.fromDomain(
                processarService.executar(request.locacaoId(), request.valor(), request.metodo(), operador));
    }

    @PostMapping("/{id}/estornar")
    @Operation(summary = "Estornar pagamento",
               description = "Estorna um pagamento aprovado. Apenas pagamentos com status APROVADO podem ser estornados.")
    @ApiResponse(responseCode = "200", description = "Pagamento estornado com sucesso")
    @ApiResponse(responseCode = "400", description = "Pagamento não pode ser estornado no status atual")
    @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    public PagamentoResponse estornar(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) EstornoRequest request,
            Authentication auth) {
        String motivo = request != null ? request.motivo() : null;
        return PagamentoResponse.fromDomain(estornarService.executar(id, motivo));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pagamento por ID")
    @ApiResponse(responseCode = "200", description = "Pagamento encontrado")
    @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    public PagamentoResponse buscarPorId(@PathVariable Long id) {
        return PagamentoResponse.fromDomain(queryService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar pagamentos",
               description = "Lista pagamentos paginados. Filtre por locacaoId ou status via query params.")
    @ApiResponse(responseCode = "200", description = "Lista de pagamentos")
    public Page<PagamentoResponse> listar(
            @RequestParam(required = false) Long locacaoId,
            @RequestParam(required = false) StatusPagamento status,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        if (locacaoId != null) {
            return queryService.listarPorLocacao(locacaoId, pageable).map(PagamentoResponse::fromDomain);
        }
        if (status != null) {
            return queryService.listarPorStatus(status, pageable).map(PagamentoResponse::fromDomain);
        }
        return queryService.listarTodos(pageable).map(PagamentoResponse::fromDomain);
    }
}
