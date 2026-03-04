package com.jtarcio.closetrental.modules.auth.application;

import com.jtarcio.closetrental.modules.auth.infrastructure.persistence.RefreshTokenEntity;
import com.jtarcio.closetrental.modules.auth.infrastructure.persistence.RefreshTokenRepositoryJpa;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Gerencia a criação, validação e revogação de refresh tokens.
 * Tokens são opacos (UUID), armazenados no banco com TTL configurável.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration:604800}") // 7 dias em segundos
    private long refreshExpirationSeconds;

    private final RefreshTokenRepositoryJpa repositoryJpa;

    @Transactional
    public String criarRefreshToken(String username) {
        // Remove tokens anteriores do usuário (single session por usuário)
        repositoryJpa.deleteByUsername(username);

        RefreshTokenEntity token = RefreshTokenEntity.builder()
                .token(UUID.randomUUID().toString())
                .username(username)
                .expiraEm(LocalDateTime.now().plusSeconds(refreshExpirationSeconds))
                .revogado(false)
                .build();

        return repositoryJpa.save(token).getToken();
    }

    @Transactional(readOnly = true)
    public RefreshTokenEntity validar(String token) {
        RefreshTokenEntity entity = repositoryJpa.findByToken(token)
                .orElseThrow(() -> new BusinessException("Refresh token inválido"));

        if (entity.isRevogado()) {
            throw new BusinessException("Refresh token revogado");
        }
        if (entity.isExpirado()) {
            throw new BusinessException("Refresh token expirado");
        }

        return entity;
    }

    @Transactional
    public void revogarPorUsername(String username) {
        repositoryJpa.deleteByUsername(username);
        log.info("Refresh tokens revogados para: {}", username);
    }

    /**
     * Limpeza automática de tokens expirados — roda toda madrugada às 3h.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void limparTokensExpirados() {
        repositoryJpa.deleteExpired(LocalDateTime.now());
        log.info("Limpeza de refresh tokens expirados concluída.");
    }
}
