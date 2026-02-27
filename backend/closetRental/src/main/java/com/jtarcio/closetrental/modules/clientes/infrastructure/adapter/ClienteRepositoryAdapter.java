package com.jtarcio.closetrental.modules.clientes.infrastructure.adapter;

import com.jtarcio.closetrental.modules.clientes.domain.model.Cliente;
import com.jtarcio.closetrental.modules.clientes.domain.ports.ClienteRepositoryPort;
import com.jtarcio.closetrental.modules.clientes.infrastructure.mapper.ClienteMapper;
import com.jtarcio.closetrental.modules.clientes.infrastructure.persistence.ClienteRepositoryJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter que implementa o port de domínio usando JPA + MapStruct.
 * Isola o domain de qualquer dependência de infraestrutura.
 */
@Component
@RequiredArgsConstructor
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {

    private final ClienteRepositoryJpa jpa;
    private final ClienteMapper mapper;

    @Override
    public Cliente salvar(Cliente cliente) {
        return mapper.toDomain(jpa.save(mapper.toEntity(cliente)));
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String documento) {
        return jpa.findByDocumento(documento).map(mapper::toDomain);
    }

    @Override
    public Page<Cliente> listarTodos(Pageable pageable) {
        return jpa.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public void deletar(Long id) {
        jpa.deleteById(id);
    }

    @Override
    public boolean existePorEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public boolean existePorEmailEIdDiferente(String email, Long id) {
        return jpa.existsByEmailAndIdNot(email, id);
    }

    @Override
    public boolean existePorDocumento(String documento) {
        return jpa.existsByDocumento(documento);
    }

    @Override
    public boolean existePorDocumentoEIdDiferente(String documento, Long id) {
        return jpa.existsByDocumentoAndIdNot(documento, id);
    }

    @Override
    public boolean possuiLocacaoAtiva(Long clienteId) {
        // Stub seguro: retorna false enquanto módulo locações não existe.
        // TODO: Implementar query real quando LocacaoEntity for criada.
        return false;
    }
}
