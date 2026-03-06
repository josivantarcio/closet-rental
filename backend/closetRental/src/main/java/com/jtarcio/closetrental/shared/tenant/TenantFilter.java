package com.jtarcio.closetrental.shared.tenant;

import com.jtarcio.closetrental.shared.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Extrai o tenantId do JWT (claim "tenantId") e popula o TenantContext.
 * SUPER_ADMIN pode sobrescrever o tenant alvo via header X-Tenant-Id.
 * Executa ANTES do JwtAuthenticationFilter para que o contexto esteja disponível.
 * Garante limpeza do ThreadLocal no finally para evitar vazamento entre requests.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Tentar extrair tenantId do header X-Tenant-Id (SUPER_ADMIN override)
            String headerTenantId = request.getHeader(TENANT_HEADER);
            if (headerTenantId != null && !headerTenantId.isBlank()) {
                try {
                    TenantContext.setTenantId(UUID.fromString(headerTenantId));
                    log.debug("TenantContext definido via header: {}", headerTenantId);
                    filterChain.doFilter(request, response);
                    return;
                } catch (IllegalArgumentException e) {
                    log.warn("X-Tenant-Id inválido: {}", headerTenantId);
                }
            }

            // 2. Extrair tenantId do JWT
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    UUID tenantId = jwtService.extrairTenantId(token);
                    if (tenantId != null) {
                        TenantContext.setTenantId(tenantId);
                        log.debug("TenantContext definido via JWT: {}", tenantId);
                    }
                } catch (Exception e) {
                    log.debug("Não foi possível extrair tenantId do token: {}", e.getMessage());
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
