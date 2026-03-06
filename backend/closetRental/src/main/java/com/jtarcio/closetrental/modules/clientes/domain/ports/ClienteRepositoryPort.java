package com.jtarcio.closetrental.modules.clientes.domain.ports;

import com.jtarcio.closetrental.modules.clientes.domain.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) de repositório de Cliente — define o contrato que
 * a infraestrutura deve implementar. O domain nunca vaza JPA.
 */
public interface ClienteRepositoryPort {

    Cliente salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(Long id);

    Optional<Cliente> buscarPorDocumento(String documento);

    Page<Cliente> listarTodos(UUID tenantId, Pageable pageable);

    void deletar(Long id);

    boolean existePorEmail(String email);

    boolean existePorEmailEIdDiferente(String email, Long id);

    boolean existePorDocumento(String documento);

    boolean existePorDocumentoEIdDiferente(String documento, Long id);

    boolean possuiLocacaoAtiva(Long clienteId);
}
