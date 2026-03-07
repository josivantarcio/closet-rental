package com.jtarcio.closetrental.modules.catalogo.application;

import com.jtarcio.closetrental.modules.catalogo.domain.model.Produto;
import com.jtarcio.closetrental.modules.catalogo.domain.ports.ImagemStoragePort;
import com.jtarcio.closetrental.modules.catalogo.domain.ports.ProdutoRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * Use Case: upload e remoção de imagem de produto.
 *
 * Regras:
 * - Aceita apenas JPEG, PNG e WebP
 * - Tamanho máximo: 5MB
 * - Gera thumbnail 400x600px (portrait) mantendo proporção
 * - Chave no storage: "produtos/{id}/{uuid}.jpg"
 * - Remove imagem anterior se existir
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImagemService {

    private static final Set<String> TIPOS_ACEITOS = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long TAMANHO_MAXIMO_BYTES = 5L * 1024 * 1024; // 5MB
    private static final int LARGURA_THUMB  = 400;
    private static final int ALTURA_THUMB   = 600;

    private final ProdutoRepositoryPort produtoPort;
    private final ImagemStoragePort storagePort;

    @Transactional
    public Produto uploadImagem(Long produtoId, MultipartFile arquivo) {
        Produto produto = produtoPort.buscarPorId(produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + produtoId));

        validarArquivo(arquivo);

        // Remove imagem anterior se houver
        if (produto.getImagemUrl() != null) {
            String chaveAntiga = extrairChave(produto.getImagemUrl());
            if (chaveAntiga != null) storagePort.deletar(chaveAntiga);
        }

        // Gera thumbnail e faz upload
        byte[] thumbnail = gerarThumbnail(arquivo);
        String chave     = "produtos/" + produtoId + "/" + java.util.UUID.randomUUID() + ".jpg";
        String url       = storagePort.upload(
                new ByteArrayInputStream(thumbnail), chave, "image/jpeg", thumbnail.length);

        produto.setImagemUrl(url);
        return produtoPort.salvar(produto);
    }

    @Transactional
    public Produto removerImagem(Long produtoId) {
        Produto produto = produtoPort.buscarPorId(produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + produtoId));

        if (produto.getImagemUrl() != null) {
            String chave = extrairChave(produto.getImagemUrl());
            if (chave != null) storagePort.deletar(chave);
            produto.setImagemUrl(null);
            return produtoPort.salvar(produto);
        }
        return produto;
    }

    // ── privados ──────────────────────────────────────────────────────────────

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BusinessException("Arquivo de imagem não informado");
        }
        String contentType = arquivo.getContentType();
        if (contentType == null || !TIPOS_ACEITOS.contains(contentType)) {
            throw new BusinessException("Tipo de imagem inválido. Aceito: JPEG, PNG, WebP");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new BusinessException("Imagem muito grande. Máximo: 5MB");
        }
    }

    private byte[] gerarThumbnail(MultipartFile arquivo) {
        try {
            BufferedImage original = ImageIO.read(arquivo.getInputStream());
            if (original == null) {
                throw new BusinessException("Não foi possível ler a imagem. Verifique o formato.");
            }
            // Redimensiona para caber em 400x600 mantendo proporção (fit inside)
            BufferedImage thumb = Scalr.resize(original, Scalr.Method.QUALITY,
                    Scalr.Mode.FIT_TO_WIDTH, LARGURA_THUMB, ALTURA_THUMB);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(thumb, "jpg", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("Erro ao processar imagem: " + e.getMessage());
        }
    }

    /**
     * Extrai a chave relativa da URL (tudo depois do baseUrl).
     * Ex: "http://localhost:8080/uploads/produtos/10/abc.jpg" → "produtos/10/abc.jpg"
     */
    private String extrairChave(String url) {
        if (url == null) return null;
        int idx = url.indexOf("/produtos/");
        return idx >= 0 ? url.substring(idx + 1) : null;
    }
}
