package com.jtarcio.closetrental.modules.usuarios.domain.ports;

import com.jtarcio.closetrental.modules.usuarios.domain.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UsuarioRepositoryPort {

    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorUsername(String username);

    boolean existePorUsername(String username);

    boolean existePorEmail(String email);

    Page<Usuario> listarTodos(Pageable pageable);

    void deletar(Long id);
}
