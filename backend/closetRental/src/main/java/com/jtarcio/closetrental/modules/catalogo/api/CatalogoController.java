package com.jtarcio.closetrental.modules.catalogo.api;

import com.jtarcio.closetrental.modules.catalogo.api.dto.PeriodoOcupadoResponse;
import com.jtarcio.closetrental.modules.catalogo.api.dto.ProdutoRequest;
import com.jtarcio.closetrental.modules.catalogo.api.dto.ProdutoResponse;
import com.jtarcio.closetrental.modules.catalogo.application.AgendaProdutoService;
import com.jtarcio.closetrental.modules.catalogo.application.ImagemService;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/v1/catalogo")
@RequiredArgsConstructor
@Tag(name = "Catálogo", description = "Gerenciamento de produtos do catálogo de aluguel")
public class CatalogoController {

    private final ProdutoService produtoService;
    private final AgendaProdutoService agendaProdutoService;
    private final ImagemService imagemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar produto", description = "Cadastra um novo produto no catálogo. Requer ADMIN ou ATENDENTE.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Produto cadastrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "403", description = "Sem permissão — exige ADMIN ou ATENDENTE")
    })
    public ProdutoResponse criar(@Valid @RequestBody ProdutoRequest request) {
        return toResponse(produtoService.criar(toDomain(request)));
    }

    @GetMapping
    @Operation(summary = "Listar produtos", description = "Lista produtos com paginação. Endpoint público.")
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
    @Operation(summary = "Atualizar produto", description = "Atualiza dados de um produto existente. Requer ADMIN ou ATENDENTE.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produto atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ProdutoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest request) {
        return toResponse(produtoService.atualizar(id, toDomain(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir produto", description = "Exclui um produto sem locações ativas. Requer ADMIN.")
    public void deletar(@PathVariable Long id) {
        produtoService.deletar(id);
    }

    @PostMapping(value = "/{id}/imagem", consumes = "multipart/form-data")
    @Operation(summary = "Upload de imagem do produto",
               description = "Faz upload da imagem (JPEG, PNG ou WebP, máx. 5MB). Gera thumbnail 400×600px. Requer ADMIN ou ATENDENTE.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Imagem enviada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Arquivo inválido (tipo ou tamanho)"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ProdutoResponse uploadImagem(
            @PathVariable Long id,
            @RequestParam("arquivo") MultipartFile arquivo) {
        return toResponse(imagemService.uploadImagem(id, arquivo));
    }

    @DeleteMapping("/{id}/imagem")
    @Operation(summary = "Remover imagem do produto", description = "Remove a imagem do produto. Requer ADMIN.")
    @ApiResponse(responseCode = "200", description = "Imagem removida")
    public ProdutoResponse removerImagem(@PathVariable Long id) {
        return toResponse(imagemService.removerImagem(id));
    }

    @GetMapping("/{id}/agenda")
    @Operation(summary = "Calendário de ocupação do produto",
               description = "Retorna os períodos ocupados (RESERVADO ou RETIRADO) de um produto no mês informado. " +
                             "Formato do parâmetro: YYYY-MM (ex: 2026-03). Endpoint público.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de períodos ocupados no mês"),
        @ApiResponse(responseCode = "400", description = "Formato de mês inválido — use YYYY-MM"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public List<PeriodoOcupadoResponse> agenda(
            @PathVariable Long id,
            @RequestParam(defaultValue = "") String mes) {
        YearMonth yearMonth = mes.isBlank() ? YearMonth.now() : YearMonth.parse(mes);
        return agendaProdutoService.buscarAgenda(id, yearMonth);
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
