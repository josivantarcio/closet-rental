package com.jtarcio.closetrental.modules.catalogo.infrastructure.storage;

import com.jtarcio.closetrental.modules.catalogo.domain.ports.ImagemStoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;

/**
 * Adapter de armazenamento usando AWS S3 + CloudFront.
 * Ativo quando app.storage.provider=s3 (padrão em produção).
 *
 * A URL retornada aponta para o CloudFront (se configurado) ou direto para o S3.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3")
public class S3ImagemStorageAdapter implements ImagemStoragePort {

    private final S3Client s3;
    private final String bucket;
    private final String baseUrl;

    public S3ImagemStorageAdapter(
            @Value("${app.storage.s3.region:us-east-1}") String region,
            @Value("${app.storage.s3.bucket}") String bucket,
            @Value("${app.storage.s3.access-key}") String accessKey,
            @Value("${app.storage.s3.secret-key}") String secretKey,
            @Value("${app.storage.s3.base-url:}") String baseUrl) {

        this.bucket  = bucket;
        this.baseUrl = baseUrl.isBlank()
                ? "https://" + bucket + ".s3." + region + ".amazonaws.com"
                : baseUrl;

        this.s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    @Override
    public String upload(InputStream inputStream, String chave, String contentType, long tamanhoBytes) {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(chave)
                .contentType(contentType)
                .contentLength(tamanhoBytes)
                .build();

        s3.putObject(req, RequestBody.fromInputStream(inputStream, tamanhoBytes));
        String url = baseUrl + "/" + chave;
        log.info("Imagem enviada para S3: chave={}, url={}", chave, url);
        return url;
    }

    @Override
    public void deletar(String chave) {
        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(chave).build());
            log.info("Imagem removida do S3: chave={}", chave);
        } catch (Exception e) {
            log.warn("Falha ao remover imagem do S3: chave={}, erro={}", chave, e.getMessage());
        }
    }
}
