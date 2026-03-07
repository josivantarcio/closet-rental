package com.jtarcio.closetrental.modules.locacoes.application;

import com.jtarcio.closetrental.modules.locacoes.domain.model.ItemLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.modules.tenants.domain.model.Tenant;
import com.jtarcio.closetrental.modules.tenants.domain.ports.TenantRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Use Case: Cancelar uma reserva com política de multa graduada.
 *
 * Regras:
 * 1. Locação deve existir
 * 2. Apenas locações com status RESERVADO podem ser canceladas
 * 3. Calcula multa de cancelamento com base na política do tenant
 *    - >= 7 dias antes da retirada → isento
 *    - 3–6 dias → percentual configurável (default 30%)
 *    - 1–2 dias → percentual configurável (default 70%)
 *    - 0 dias (mesmo dia) → percentual configurável (default 100%)
 * 4. Base de cálculo: soma dos subtotais dos itens (sem caução)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CancelarReservaService {

    private final LocacaoRepositoryPort locacaoPort;
    private final TenantRepositoryPort tenantPort;

    @Transactional
    public Locacao executar(Long locacaoId, String motivoCancelamento) {
        Locacao locacao = locacaoPort.buscarPorId(locacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Locacao nao encontrada com id: " + locacaoId));

        if (!locacao.podeCancelar()) {
            throw new BusinessException(
                "Nao e possivel cancelar uma locacao com status: " + locacao.getStatus() +
                ". Apenas locacoes com status RESERVADO podem ser canceladas.");
        }

        // Calcula multa de cancelamento usando a política do tenant
        BigDecimal multaCancelamento = calcularMultaCancelamento(locacao);
        locacao.setMultaCancelamento(multaCancelamento);
        locacao.setStatus(StatusLocacao.CANCELADO);

        if (motivoCancelamento != null && !motivoCancelamento.isBlank()) {
            locacao.setObservacoes(motivoCancelamento);
        }

        Locacao salva = locacaoPort.salvar(locacao);
        log.info("Reserva cancelada: locacaoId={}, multaCancelamento={}", salva.getId(), multaCancelamento);
        return salva;
    }

    /**
     * Busca a política do tenant e calcula a multa.
     * Base = soma dos subtotais dos itens (excluindo caução).
     * Se o tenant não for encontrado, usa a política padrão (defaults do Tenant).
     * Complexidade: O(n) onde n = número de itens (tipicamente pequeno).
     */
    private BigDecimal calcularMultaCancelamento(Locacao locacao) {
        UUID tenantId = locacao.getTenantId();

        Tenant tenant = tenantId != null
                ? tenantPort.buscarPorId(tenantId).orElse(new Tenant())
                : new Tenant();

        // Base = soma dos subtotais dos itens (valorDiaria * dias de cada item)
        long dias = locacao.calcularDias();
        BigDecimal baseCalculo = locacao.getItens().stream()
                .map(item -> item.calcularSubtotal(dias))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return tenant.calcularMultaCancelamento(locacao.getDataInicio(), baseCalculo, LocalDate.now());
    }
}
