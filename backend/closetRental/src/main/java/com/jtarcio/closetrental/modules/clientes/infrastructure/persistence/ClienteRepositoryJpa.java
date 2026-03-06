package com.jtarcio.closetrental.modules.clientes.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório JPA de Cliente. Queries usam índices únicos — O(1) por documento/email.
 * Nota: possuiLocacaoAtiva é tratado no adapter com stub seguro até o módulo locações existir.
 */
public interface ClienteRepositoryJpa extends JpaRepository<ClienteEntity, Long> {

    Optional<ClienteEntity> findByDocumento(String documento);

    Page<ClienteEntity> findByTenantId(UUID tenantId, Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdNot(String documento, Long id);
}
