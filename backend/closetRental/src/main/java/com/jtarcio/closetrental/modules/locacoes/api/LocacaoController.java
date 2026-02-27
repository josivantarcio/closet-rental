package com.jtarcio.closetrental.modules.locacoes.api;

import com.jtarcio.closetrental.modules.locacoes.api.dto.*;
import com.jtarcio.closetrental.modules.locacoes.application.*;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para o modulo de Locacoes.
 * Endpoint base: /v1/locacoes
 */
@RestController
@RequestMapping("/v1/locacoes")
@RequiredArgsConstructor
@Tag(name = "Locacoes", description = "Gestao do ciclo de locacao: reserva, retirada, devolucao e cancelamento")
public class LocacaoController {

    private final CriarReservaService criarReservaService;
    private final RegistrarRetiradaService registrarRetiradaService;
    private final RegistrarDevolucaoService registrarDevolucaoService;
    private final CancelarReservaService cancelarReservaService;
    private final LocacaoQueryService queryService;

    @PostMapping("/reservar")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar reserva",
               description = "Cria uma nova reserva. Valida disponibilidade do produto no periodo, sem overlap.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Reserva criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados invalidos ou datas incorretas"),
        @ApiResponse(responseCode = "404", description = "Cliente ou produto nao encontrado"),
        @ApiResponse(responseCode = "409", description = "Produto ja reservado no periodo informado")
    })
    public LocacaoResponse reservar(@Valid @RequestBody ReservaRequest request) {
        return LocacaoResponse.fromDomain(
            criarReservaService.executar(
                request.clienteId(),
                request.produtoId(),
                request.dataInicio(),
                request.dataFim(),
                request.caucao(),
                request.observacoes()
            )
        );
    }

    @PostMapping("/{id}/retirar")
    @Operation(summary = "Registrar retirada",
               description = "Registra a retirada da peca. Locacao deve estar no status RESERVADO.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Retirada registrada"),
        @ApiResponse(responseCode = "400", description = "Locacao nao esta no status RESERVADO"),
        @ApiResponse(responseCode = "404", description = "Locacao nao encontrada")
    })
    public LocacaoResponse retirar(@PathVariable Long id) {
        return LocacaoResponse.fromDomain(registrarRetiradaService.executar(id));
    }

    @PostMapping("/{id}/devolver")
    @Operation(summary = "Registrar devolucao",
               description = "Registra a devolucao da peca. Calcula multa se houver atraso. Locacao deve estar no status RETIRADO.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Devolucao registrada"),
        @ApiResponse(responseCode = "400", description = "Locacao nao esta no status RETIRADO"),
        @ApiResponse(responseCode = "404", description = "Locacao nao encontrada")
    })
    public LocacaoResponse devolver(@PathVariable Long id,
                                    @RequestBody(required = false) DevolucaoRequest request) {
        String obs = (request != null) ? request.observacoes() : null;
        return LocacaoResponse.fromDomain(registrarDevolucaoService.executar(id, obs));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar reserva",
               description = "Cancela a reserva. Apenas locacoes com status RESERVADO podem ser canceladas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reserva cancelada"),
        @ApiResponse(responseCode = "400", description = "Locacao nao pode ser cancelada no status atual"),
        @ApiResponse(responseCode = "404", description = "Locacao nao encontrada")
    })
    public LocacaoResponse cancelar(@PathVariable Long id,
                                    @RequestBody(required = false) CancelamentoRequest request) {
        String motivo = (request != null) ? request.motivo() : null;
        return LocacaoResponse.fromDomain(cancelarReservaService.executar(id, motivo));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar locacao por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Locacao encontrada"),
        @ApiResponse(responseCode = "404", description = "Locacao nao encontrada")
    })
    public LocacaoResponse buscarPorId(@PathVariable Long id) {
        return LocacaoResponse.fromDomain(queryService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar locacoes",
               description = "Lista todas as locacoes com paginacao. Filtre por clienteId ou status via query params.")
    public Page<LocacaoResponse> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) StatusLocacao status,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        if (clienteId != null) {
            return queryService.listarPorCliente(clienteId, pageable).map(LocacaoResponse::fromDomain);
        }
        if (status != null) {
            return queryService.listarPorStatus(status, pageable).map(LocacaoResponse::fromDomain);
        }
        return queryService.listarTodos(pageable).map(LocacaoResponse::fromDomain);
    }
}
