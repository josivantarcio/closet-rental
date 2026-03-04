package com.jtarcio.closetrental.modules.usuarios.application;

import com.jtarcio.closetrental.modules.usuarios.domain.model.Usuario;
import com.jtarcio.closetrental.modules.usuarios.domain.ports.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementação de UserDetailsService que carrega usuários do banco de dados.
 * Substitui o InMemoryUserDetailsConfig.
 */
@Service("userDetailsService")
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepositoryPort repositoryPort;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = repositoryPort.buscarPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        if (!usuario.isAtivo()) {
            throw new UsernameNotFoundException("Usuário inativo: " + username);
        }

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getSenhaHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRole())))
                .build();
    }
}
