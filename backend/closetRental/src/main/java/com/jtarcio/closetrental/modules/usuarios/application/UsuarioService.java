package com.jtarcio.closetrental.modules.usuarios.application;

import com.jtarcio.closetrental.modules.usuarios.domain.model.Usuario;
import com.jtarcio.closetrental.modules.usuarios.domain.ports.UsuarioRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.ConflictException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final Set<String> ROLES_VALIDAS = Set.of("SUPER_ADMIN", "ADMIN", "ATENDENTE", "CLIENTE");

    private final UsuarioRepositoryPort repositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario criar(Usuario usuario, String senhaPlana) {
        validarRole(usuario.getRole());
        if (repositoryPort.existePorUsername(usuario.getUsername())) {
            throw new ConflictException("Username já cadastrado: " + usuario.getUsername());
        }
        if (repositoryPort.existePorEmail(usuario.getEmail())) {
            throw new ConflictException("E-mail já cadastrado: " + usuario.getEmail());
        }
        usuario.setSenhaHash(passwordEncoder.encode(senhaPlana));
        usuario.setAtivo(true);
        log.info("Criando usuário: {} role={}", usuario.getUsername(), usuario.getRole());
        return repositoryPort.salvar(usuario);
    }

    @Transactional(readOnly = true)
    public Page<Usuario> listar(Pageable pageable) {
        return repositoryPort.listarTodos(pageable);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));
    }

    @Transactional
    public Usuario atualizar(Long id, Usuario dados) {
        Usuario existente = buscarPorId(id);
        validarRole(dados.getRole());
        existente.setNome(dados.getNome());
        existente.setEmail(dados.getEmail());
        existente.setRole(dados.getRole());
        log.info("Atualizando usuário id={}", id);
        return repositoryPort.salvar(existente);
    }

    @Transactional
    public void alterarSenha(Long id, String senhaAtual, String novaSenha) {
        Usuario existente = buscarPorId(id);
        if (!passwordEncoder.matches(senhaAtual, existente.getSenhaHash())) {
            throw new BusinessException("Senha atual incorreta");
        }
        existente.setSenhaHash(passwordEncoder.encode(novaSenha));
        repositoryPort.salvar(existente);
        log.info("Senha alterada para usuário id={}", id);
    }

    @Transactional
    public void inativar(Long id) {
        Usuario existente = buscarPorId(id);
        existente.inativar();
        repositoryPort.salvar(existente);
        log.info("Usuário inativado id={}", id);
    }

    @Transactional
    public void reativar(Long id) {
        Usuario existente = buscarPorId(id);
        existente.reativar();
        repositoryPort.salvar(existente);
        log.info("Usuário reativado id={}", id);
    }

    @Transactional
    public void deletar(Long id) {
        buscarPorId(id);
        log.info("Deletando usuário id={}", id);
        repositoryPort.deletar(id);
    }

    private void validarRole(String role) {
        if (!ROLES_VALIDAS.contains(role)) {
            throw new BusinessException("Role inválida. Permitidas: " + ROLES_VALIDAS);
        }
    }
}
