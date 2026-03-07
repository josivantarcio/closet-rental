package com.jtarcio.closetrental.modules.catalogo.infrastructure.storage;

import com.jtarcio.closetrental.modules.catalogo.domain.ports.ImagemStoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Adapter de armazenamento local para desenvolvimento/teste.
 * Ativo quando app.storage.provider=local (padrão).
 *
 * Salva os arquivos em {app.storage.local.path} e retorna uma URL relativa
 * que o backend serve via /uploads/** (configurado no WebMvcConfig).
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalImagemStorageAdapter implements ImagemStoragePort {

    private final Path uploadDir;
    private final String baseUrl;

    public LocalImagemStorageAdapter(
            @Value("${app.storage.local.path:uploads}") String uploadPath,
            @Value("${app.storage.local.base-url:http://localhost:8080/uploads}") String baseUrl) {

        this.uploadDir = Paths.get(uploadPath).toAbsolutePath();
        this.baseUrl   = baseUrl;

        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível criar diretório de uploads: " + uploadDir, e);
        }
        log.info("LocalImagemStorageAdapter: diretório={}, baseUrl={}", uploadDir, baseUrl);
    }

    @Override
    public String upload(InputStream inputStream, String chave, String contentType, long tamanhoBytes) {
        try {
            Path destino = uploadDir.resolve(chave);
            Files.createDirectories(destino.getParent());
            Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
            String url = baseUrl + "/" + chave;
            log.info("Imagem salva localmente: path={}, url={}", destino, url);
            return url;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar imagem localmente: " + chave, e);
        }
    }

    @Override
    public void deletar(String chave) {
        try {
            Path arquivo = uploadDir.resolve(chave);
            Files.deleteIfExists(arquivo);
            log.info("Imagem local removida: path={}", arquivo);
        } catch (IOException e) {
            log.warn("Falha ao remover imagem local: chave={}, erro={}", chave, e.getMessage());
        }
    }
}
