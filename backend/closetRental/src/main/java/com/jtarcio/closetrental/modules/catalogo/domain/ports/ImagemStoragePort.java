package com.jtarcio.closetrental.modules.catalogo.domain.ports;

import java.io.InputStream;

/**
 * Port de armazenamento de imagens.
 * Implementações: S3ImagemStorageAdapter (produção) e LocalImagemStorageAdapter (dev).
 */
public interface ImagemStoragePort {

    /**
     * Faz upload de uma imagem e retorna a URL pública.
     *
     * @param inputStream  conteúdo da imagem
     * @param chave        caminho/nome do arquivo no storage (ex: "produtos/10/foto.jpg")
     * @param contentType  MIME type (ex: "image/jpeg")
     * @param tamanhoBytes tamanho em bytes
     * @return URL pública da imagem
     */
    String upload(InputStream inputStream, String chave, String contentType, long tamanhoBytes);

    /**
     * Remove uma imagem do storage pela chave.
     * Não lança exceção se a imagem não existir.
     */
    void deletar(String chave);
}
