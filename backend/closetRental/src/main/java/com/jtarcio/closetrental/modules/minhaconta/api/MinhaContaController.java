package com.jtarcio.closetrental.modules.minhaconta.api;

import com.jtarcio.closetrental.modules.locacoes.api.dto.LocacaoResponse;
import com.jtarcio.closetrental.modules.locacoes.application.ContratoService;
import com.jtarcio.closetrental.modules.minhaconta.api.dto.AlterarMinhaSenhaRequest;
import com.jtarcio.closetrental.modules.minhaconta.api.dto.PerfilResponse;
import com.jtarcio.closetrental.modules.minhaconta.application.MinhaContaService;
import com.jtarcio.closetrental.modules.pagamentos.api.dto.PagamentoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST do portal self-service do cliente.
 * Endpoint base: /v1/minha-conta
 *
 * Disponível para ROLE_CLIENTE (e superiores).
 * Todas as operações são filtradas pelo usuário autenticado — sem risco de acesso cruzado.
 */
@RestController
@RequestMapping("/v1/minha-conta")
@RequiredArgsConstructor
@Tag(name = "Minha Conta", description = "Portal self-service do cliente — acesso restrito ao próprio usuário")
public class MinhaContaController {

    private final MinhaContaService minhaContaService;
    private final ContratoService contratoService;

    @GetMapping("/perfil")
    @Operation(summary = "Meu perfil", description = "Retorna os dados do usuário autenticado.")
    @ApiResponse(responseCode = "200", description = "Perfil retornado")
    public PerfilResponse perfil(Authentication auth) {
        return PerfilResponse.fromDomain(minhaContaService.buscarPerfil(auth.getName()));
    }

    @PatchMapping("/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Alterar minha senha", description = "Altera a senha do usuário autenticado.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Senha alterada"),
        @ApiResponse(responseCode = "400", description = "Senha atual incorreta ou nova senha inválida")
    })
    public void alterarSenha(Authentication auth,
                              @Valid @RequestBody AlterarMinhaSenhaRequest request) {
        minhaContaService.alterarSenha(auth.getName(), request.senhaAtual(), request.novaSenha());
    }

    @GetMapping("/locacoes")
    @Operation(summary = "Minhas locações", description = "Lista as locações do cliente autenticado com paginação.")
    @ApiResponse(responseCode = "200", description = "Lista paginada de locações")
    public Page<LocacaoResponse> minhasLocacoes(
            Authentication auth,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return minhaContaService.minhasLocacoes(auth.getName(), pageable)
                .map(LocacaoResponse::fromDomain);
    }

    @GetMapping("/pagamentos")
    @Operation(summary = "Meus pagamentos", description = "Lista os pagamentos das locações do cliente autenticado.")
    @ApiResponse(responseCode = "200", description = "Lista paginada de pagamentos")
    public Page<PagamentoResponse> meusPagamentos(
            Authentication auth,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return minhaContaService.meusPagamentos(auth.getName(), pageable)
                .map(PagamentoResponse::fromDomain);
    }

    @GetMapping(value = "/locacoes/{id}/contrato", produces = "application/pdf")
    @Operation(summary = "Baixar contrato em PDF",
               description = "Gera o contrato de uma locação do cliente autenticado em PDF.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF gerado"),
        @ApiResponse(responseCode = "403", description = "Locação não pertence ao cliente autenticado"),
        @ApiResponse(responseCode = "404", description = "Locação não encontrada")
    })
    public ResponseEntity<byte[]> contrato(Authentication auth, @PathVariable Long id) {
        minhaContaService.validarPosse(auth.getName(), id);
        byte[] pdf = contratoService.gerarContrato(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
            ContentDisposition.attachment()
                .filename("contrato-locacao-" + id + ".pdf")
                .build()
        );
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
