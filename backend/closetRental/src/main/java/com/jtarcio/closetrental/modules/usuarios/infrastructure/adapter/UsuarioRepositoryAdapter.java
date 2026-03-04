package com.jtarcio.closetrental.modules.usuarios.infrastructure.adapter;

import com.jtarcio.closetrental.modules.usuarios.domain.model.Usuario;
import com.jtarcio.closetrental.modules.usuarios.domain.ports.UsuarioRepositoryPort;
import com.jtarcio.closetrental.modules.usuarios.infrastructure.mapper.UsuarioMapper;
import com.jtarcio.closetrental.modules.usuarios.infrastructure.persistence.UsuarioRepositoryJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioRepositoryJpa jpa;
    private final UsuarioMapper mapper;

    @Override
    public Usuario salvar(Usuario usuario) {
        return mapper.toDomain(jpa.save(mapper.toEntity(usuario)));
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return jpa.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public boolean existePorUsername(String username) {
        return jpa.existsByUsername(username);
    }

    @Override
    public boolean existePorEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public Page<Usuario> listarTodos(Pageable pageable) {
        return jpa.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public void deletar(Long id) {
        jpa.deleteById(id);
    }
}
