package com.jtarcio.closetrental.modules.clientes.application;

import com.jtarcio.closetrental.modules.clientes.domain.model.Cliente;
import com.jtarcio.closetrental.modules.clientes.domain.ports.ClienteRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.ConflictException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import com.jtarcio.closetrental.shared.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de aplicação para gestão de clientes.
 * Concentra todas as regras de negócio: unicidade, validações e restrições de exclusão.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepositoryPort repositoryPort;

    /**
     * Cria um novo cliente após validar unicidade de email e documento.
     * Complexidade: O(1) com índice único no banco.
     */
    @Transactional
    public Cliente criar(Cliente cliente) {
        validarEmailUnico(cliente.getEmail(), null);
        validarDocumentoUnico(cliente.getDocumento(), null);
        cliente.setAtivo(true);
        cliente.setTenantId(TenantContext.getTenantId());
        log.info("Criando cliente: {}", cliente.getNome());
        return repositoryPort.salvar(cliente);
    }

    /**
     * Lista todos os clientes com paginação obrigatória.
     * Nunca carrega todos os registros em memória.
     */
    @Transactional(readOnly = true)
    public Page<Cliente> listar(Pageable pageable) {
        log.info("Listando clientes — página {}", pageable.getPageNumber());
        return repositoryPort.listarTodos(TenantContext.getTenantId(), pageable);
    }

    /**
     * Busca cliente por ID. Lança {@link EntityNotFoundException} se não encontrado.
     */
    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        log.info("Buscando cliente id: {}", id);
        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com id: " + id));
    }

    /**
     * Atualiza dados do cliente sem permitir troca de documento.
     * Email pode ser alterado desde que continue único.
     */
    @Transactional
    public Cliente atualizar(Long id, Cliente dadosNovos) {
        Cliente existente = buscarPorId(id);
        validarEmailUnico(dadosNovos.getEmail(), id);

        if (!existente.getDocumento().equals(dadosNovos.getDocumento())) {
            throw new BusinessException("Não é permitido alterar o documento do cliente");
        }

        existente.setNome(dadosNovos.getNome());
        existente.setEmail(dadosNovos.getEmail());
        existente.setTelefone(dadosNovos.getTelefone());
        existente.setEndereco(dadosNovos.getEndereco());

        log.info("Atualizando cliente id: {}", id);
        return repositoryPort.salvar(existente);
    }

    /**
     * Exclui o cliente fisicamente apenas se não possuir locações ativas.
     * Clientes com histórico devem ser inativados, não deletados.
     */
    @Transactional
    public void deletar(Long id) {
        buscarPorId(id);
        if (repositoryPort.possuiLocacaoAtiva(id)) {
            throw new BusinessException("Não é possível excluir cliente com locações ativas. Inative-o em vez disso.");
        }
        log.info("Deletando cliente id: {}", id);
        repositoryPort.deletar(id);
    }

    /**
     * Inativa o cliente (soft delete) sem excluí-lo do banco.
     * Preserva o histórico de locações.
     */
    @Transactional
    public Cliente inativar(Long id) {
        Cliente cliente = buscarPorId(id);
        if (!cliente.isAtivo()) {
            throw new BusinessException("Cliente já está inativo");
        }
        cliente.inativar();
        log.info("Inativando cliente id: {}", id);
        return repositoryPort.salvar(cliente);
    }

    /**
     * Reativa um cliente previamente inativado.
     */
    @Transactional
    public Cliente reativar(Long id) {
        Cliente cliente = buscarPorId(id);
        if (cliente.isAtivo()) {
            throw new BusinessException("Cliente já está ativo");
        }
        cliente.reativar();
        log.info("Reativando cliente id: {}", id);
        return repositoryPort.salvar(cliente);
    }

    // --- Validações privadas ---

    private void validarEmailUnico(String email, Long idExistente) {
        boolean existe = idExistente == null
                ? repositoryPort.existePorEmail(email)
                : repositoryPort.existePorEmailEIdDiferente(email, idExistente);
        if (existe) {
            throw new ConflictException("Já existe um cliente cadastrado com o email: " + email);
        }
    }

    private void validarDocumentoUnico(String documento, Long idExistente) {
        boolean existe = idExistente == null
                ? repositoryPort.existePorDocumento(documento)
                : repositoryPort.existePorDocumentoEIdDiferente(documento, idExistente);
        if (existe) {
            throw new ConflictException("Já existe um cliente cadastrado com o documento: " + documento);
        }
    }
}
