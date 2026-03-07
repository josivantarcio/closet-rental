package com.jtarcio.closetrental.modules.usuarios.api;

import com.jtarcio.closetrental.modules.usuarios.api.dto.AlterarSenhaRequest;
import com.jtarcio.closetrental.modules.usuarios.api.dto.UsuarioRequest;
import com.jtarcio.closetrental.modules.usuarios.api.dto.UsuarioResponse;
import com.jtarcio.closetrental.modules.usuarios.application.UsuarioService;
import com.jtarcio.closetrental.modules.usuarios.domain.model.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST do módulo de Usuários.
 * Endpoint base: /v1/usuarios
 * Todos os endpoints exigem ROLE_ADMIN (configurado no SecurityConfig).
 */
@RestController
@RequestMapping("/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento de usuários do sistema (somente ADMIN)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar usuário", description = "Cria novo usuário. Requer ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuário criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Username ou e-mail já existe")
    })
    public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest request) {
        Usuario usuario = toDomain(request);
        return toResponse(usuarioService.criar(usuario, request.senha()));
    }

    @GetMapping
    @Operation(summary = "Listar usuários", description = "Lista usuários com paginação. Requer ADMIN.")
    @ApiResponse(responseCode = "200", description = "Lista paginada")
    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarioService.listar(pageable).map(this::toResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
        @ApiResponse(responseCode = "404", description = "Não encontrado")
    })
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        return toResponse(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza nome, e-mail e role. Requer ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário atualizado"),
        @ApiResponse(responseCode = "404", description = "Não encontrado")
    })
    public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return toResponse(usuarioService.atualizar(id, toDomain(request)));
    }

    @PatchMapping("/{id}/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Alterar senha", description = "Altera a senha do usuário. Requer ADMIN.")
    public void alterarSenha(@PathVariable Long id, @Valid @RequestBody AlterarSenhaRequest request) {
        usuarioService.alterarSenha(id, request.senhaAtual(), request.novaSenha());
    }

    @PatchMapping("/{id}/inativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Inativar usuário")
    public void inativar(@PathVariable Long id) {
        usuarioService.inativar(id);
    }

    @PatchMapping("/{id}/reativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Reativar usuário")
    public void reativar(@PathVariable Long id) {
        usuarioService.reativar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar usuário", description = "Remove usuário permanentemente. Requer ADMIN.")
    public void deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
    }

    private Usuario toDomain(UsuarioRequest request) {
        Usuario u = new Usuario();
        u.setNome(request.nome());
        u.setUsername(request.username());
        u.setEmail(request.email());
        u.setRole(request.role());
        u.setClienteId(request.clienteId());
        return u;
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getNome(), u.getUsername(),
                u.getEmail(), u.getRole(), u.isAtivo(), u.getClienteId(), u.getCreatedAt()
        );
    }
}
