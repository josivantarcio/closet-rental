package com.jtarcio.closetrental.modules.catalogo.api;

import com.jtarcio.closetrental.modules.catalogo.api.dto.ProdutoRequest;
import com.jtarcio.closetrental.modules.catalogo.api.dto.ProdutoResponse;
import com.jtarcio.closetrental.modules.catalogo.application.ProdutoService;
import com.jtarcio.closetrental.modules.catalogo.domain.model.Produto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/catalogo")
@RequiredArgsConstructor
@Tag(name = "Catálogo", description = "Gerenciamento de produtos do catálogo")
public class CatalogoController {

    private final ProdutoService produtoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResponse criar(@Valid @RequestBody ProdutoRequest request) {
        Produto produto = toDomain(request);
        return toResponse(produtoService.criar(produto));
    }

    @GetMapping
    public Page<ProdutoResponse> listar(Pageable pageable) {
        return produtoService.listar(pageable).map(this::toResponse);
    }

    @GetMapping("/{id}")
    public ProdutoResponse buscarPorId(@PathVariable Long id) {
        return toResponse(produtoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ProdutoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest request) {
        Produto produto = toDomain(request);
        return toResponse(produtoService.atualizar(id, produto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        produtoService.deletar(id);
    }

    private Produto toDomain(ProdutoRequest request) {
        Produto produto = new Produto();
        produto.setNome(request.nome());
        produto.setCategoria(request.categoria());
        produto.setValorDiaria(request.valorDiaria());
        produto.setDisponivel(request.disponivel());
        return produto;
    }

    private ProdutoResponse toResponse(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getCategoria(),
                produto.getValorDiaria(),
                produto.isDisponivel(),
                produto.getCreatedBy(),
                produto.getCreatedAt()
        );
    }
}
