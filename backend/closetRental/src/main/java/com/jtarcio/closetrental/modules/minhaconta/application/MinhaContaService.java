package com.jtarcio.closetrental.modules.minhaconta.application;

import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.Pagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.ports.PagamentoRepositoryPort;
import com.jtarcio.closetrental.modules.usuarios.domain.model.Usuario;
import com.jtarcio.closetrental.modules.usuarios.domain.ports.UsuarioRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import com.jtarcio.closetrental.shared.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Serviço do portal self-service do cliente.
 * Todas as operações são restritas aos dados do usuário logado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinhaContaService {

    private final UsuarioRepositoryPort usuarioPort;
    private final LocacaoRepositoryPort locacaoPort;
    private final PagamentoRepositoryPort pagamentoPort;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Usuario buscarPerfil(String username) {
        return usuarioPort.buscarPorUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + username));
    }

    @Transactional
    public void alterarSenha(String username, String senhaAtual, String novaSenha) {
        Usuario usuario = buscarPerfil(username);
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenhaHash())) {
            throw new BusinessException("Senha atual incorreta");
        }
        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuarioPort.salvar(usuario);
        log.info("Senha alterada pelo próprio usuário: {}", username);
    }

    @Transactional(readOnly = true)
    public Page<Locacao> minhasLocacoes(String username, Pageable pageable) {
        Usuario usuario = buscarPerfil(username);
        Long clienteId = resolverClienteId(usuario);
        return locacaoPort.listarPorCliente(TenantContext.getTenantId(), clienteId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Pagamento> meusPagamentos(String username, Pageable pageable) {
        Usuario usuario = buscarPerfil(username);
        Long clienteId = resolverClienteId(usuario);
        List<Long> locacaoIds = locacaoPort.listarIdsPorCliente(TenantContext.getTenantId(), clienteId);
        return pagamentoPort.listarPorLocacoes(locacaoIds, pageable);
    }

    /**
     * Verifica se a locação pertence ao cliente autenticado.
     * Lança 403 caso contrário — sem vazar informações de outras locações.
     */
    @Transactional(readOnly = true)
    public void validarPosse(String username, Long locacaoId) {
        Usuario usuario = buscarPerfil(username);
        Long clienteId = resolverClienteId(usuario);
        Locacao locacao = locacaoPort.buscarPorId(locacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Locação não encontrada: " + locacaoId));
        if (!clienteId.equals(locacao.getClienteId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado a esta locação");
        }
    }

    private Long resolverClienteId(Usuario usuario) {
        if (usuario.getClienteId() == null) {
            throw new BusinessException("Sua conta não está vinculada a um cadastro de cliente. Contate o administrador.");
        }
        return usuario.getClienteId();
    }
}
