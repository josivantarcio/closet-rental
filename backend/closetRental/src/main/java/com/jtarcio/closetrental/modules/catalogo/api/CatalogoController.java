package com.jtarcio.closetrental.modules.catalogo.api;

import com.jtarcio.closetrental.modules.catalogo.api.dto.ProdutoRequest;
import com.jtarcio.closetrental.modules.catalogo.api.dto.ProdutoResponse;
import com.jtarcio.closetrental.modules.catalogo.application.ProdutoService;
import com.jtarcio.closetrental.modules.catalogo.domain.model.Produto;
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
 * Controller REST do módulo de Catálogo.
 * Endpoint base: /v1/catalogo
 * GET é público; POST/PUT exigem ADMIN ou ATENDENTE; DELETE exige ADMIN.
 */
@RestController
@RequestMapping("/v1/catalogo")
@RequiredArgsConstructor
@Tag(name = "Catálogo", description = "Gerenciamento de produtos do catálogo de aluguel")
public class CatalogoController {

    private final ProdutoService produtoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar produto",
               description = "Cadastra um novo produto no catálogo. Requer ADMIN ou ATENDENTE.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Produto cadastrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos (ex: valor diária negativo)"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão — exige ADMIN ou ATENDENTE")
    })
    public ProdutoResponse criar(@Valid @RequestBody ProdutoRequest request) {
        Produto produto = toDomain(request);
        return toResponse(produtoService.criar(produto));
    }

    @GetMapping
    @Operation(summary = "Listar produtos",
               description = "Lista produtos do catálogo com paginação. Endpoint público.")
    @ApiResponse(responseCode = "200", description = "Lista paginada de produtos")
    public Page<ProdutoResponse> listar(Pageable pageable) {
        return produtoService.listar(pageable).map(this::toResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID", description = "Retorna um produto pelo ID. Endpoint público.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produto encontrado"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ProdutoResponse buscarPorId(@PathVariable Long id) {
        return toResponse(produtoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto",
               description = "Atualiza dados de um produto existente. Requer ADMIN ou ATENDENTE.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produto atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão — exige ADMIN ou ATENDENTE"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ProdutoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest request) {
        Produto produto = toDomain(request);
        return toResponse(produtoService.atualizar(id, produto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir produto",
               description = "Exclui um produto sem locações ativas. Requer ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Produto excluído"),
        @ApiResponse(responseCode = "400", description = "Produto possui locações ativas"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão — exige ADMIN"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public void deletar(@PathVariable Long id) {
        produtoService.deletar(id);
    }

    private Produto toDomain(ProdutoRequest request) {
        Produto produto = new Produto();
        produto.setNome(request.nome());
        produto.setCategoria(request.categoria());
        produto.setValorDiaria(request.valorDiaria());
        produto.setCodigo(request.codigo());
        produto.setDisponivel(request.disponivel() != null ? request.disponivel() : true);
        produto.setTamanho(request.tamanho());
        produto.setCor(request.cor());
        produto.setDescricao(request.descricao());
        produto.setImagemUrl(request.imagemUrl());
        return produto;
    }

    private ProdutoResponse toResponse(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getCategoria(),
                produto.getValorDiaria(),
                produto.getCodigo(),
                produto.isDisponivel(),
                produto.getTamanho(),
                produto.getCor(),
                produto.getDescricao(),
                produto.getImagemUrl(),
                produto.getCreatedBy(),
                produto.getCreatedAt()
        );
    }
}
