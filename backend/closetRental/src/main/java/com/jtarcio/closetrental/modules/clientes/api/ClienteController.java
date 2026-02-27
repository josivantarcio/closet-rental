package com.jtarcio.closetrental.modules.clientes.api;

import com.jtarcio.closetrental.modules.clientes.api.dto.ClienteRequest;
import com.jtarcio.closetrental.modules.clientes.api.dto.ClienteResponse;
import com.jtarcio.closetrental.modules.clientes.application.ClienteService;
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
 * Controller REST para o módulo de Clientes.
 * Todos os endpoints exigem autenticação (ADMIN ou ATENDENTE).
 */
@RestController
@RequestMapping("/v1/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gerenciamento de clientes da loja")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar cliente", description = "Cria um novo cliente. Email e documento devem ser únicos.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cliente criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Email ou documento já cadastrado")
    })
    public ClienteResponse criar(@Valid @RequestBody ClienteRequest request) {
        return ClienteResponse.fromDomain(clienteService.criar(request.toDomain()));
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Lista clientes com paginação. Padrão: 20 por página, ordenado por nome.")
    public Page<ClienteResponse> listar(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return clienteService.listar(pageable).map(ClienteResponse::fromDomain);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ClienteResponse buscarPorId(@PathVariable Long id) {
        return ClienteResponse.fromDomain(clienteService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente", description = "Atualiza dados do cliente. Documento não pode ser alterado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou tentativa de trocar documento"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
        @ApiResponse(responseCode = "409", description = "Email já em uso por outro cliente")
    })
    public ClienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return ClienteResponse.fromDomain(clienteService.atualizar(id, request.toDomain()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir cliente", description = "Exclui cliente sem locações ativas. Se tiver histórico, use PATCH /inativar.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Cliente excluído"),
        @ApiResponse(responseCode = "400", description = "Cliente com locações ativas"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public void deletar(@PathVariable Long id) {
        clienteService.deletar(id);
    }

    @PatchMapping("/{id}/inativar")
    @Operation(summary = "Inativar cliente", description = "Inativa o cliente sem excluí-lo. Preserva histórico.")
    @ApiResponse(responseCode = "200", description = "Cliente inativado")
    public ClienteResponse inativar(@PathVariable Long id) {
        return ClienteResponse.fromDomain(clienteService.inativar(id));
    }

    @PatchMapping("/{id}/reativar")
    @Operation(summary = "Reativar cliente", description = "Reativa um cliente previamente inativado.")
    @ApiResponse(responseCode = "200", description = "Cliente reativado")
    public ClienteResponse reativar(@PathVariable Long id) {
        return ClienteResponse.fromDomain(clienteService.reativar(id));
    }
}
