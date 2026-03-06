package com.jtarcio.closetrental.modules.pagamentos.domain.ports;

import com.jtarcio.closetrental.modules.pagamentos.domain.model.Pagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.StatusPagamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Port de repositório de Pagamento.
 * Define o contrato que a infraestrutura implementa;
 * o domínio nunca depende de JPA diretamente.
 */
public interface PagamentoRepositoryPort {

    Pagamento salvar(Pagamento pagamento);

    Optional<Pagamento> buscarPorId(Long id);

    Page<Pagamento> listarPorLocacao(Long locacaoId, Pageable pageable);

    Page<Pagamento> listarPorStatus(UUID tenantId, StatusPagamento status, Pageable pageable);

    Page<Pagamento> listarTodos(UUID tenantId, Pageable pageable);
}
