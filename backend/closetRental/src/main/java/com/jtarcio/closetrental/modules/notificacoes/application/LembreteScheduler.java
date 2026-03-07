package com.jtarcio.closetrental.modules.notificacoes.application;

import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.modules.notificacoes.domain.model.TipoNotificacao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scheduler de lembretes automáticos por e-mail.
 *
 * Roda diariamente às 9h (configurável via LEMBRETE_CRON).
 * Cada job é independente: uma falha não cancela os demais.
 *
 * Eventos cobertos:
 *   - LEMBRETE_RETIRADA  → locações RESERVADAS com retirada amanhã
 *   - LEMBRETE_DEVOLUCAO → locações RETIRADAS com devolução hoje
 *   - AVISO_ATRASO       → locações RETIRADAS com dataFim < hoje
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LembreteScheduler {

    private final LocacaoRepositoryPort locacaoPort;
    private final NotificacaoContextBuilder contextBuilder;
    private final NotificacaoService notificacaoService;

    /**
     * Lembrete de retirada: dispara para todas as reservas cuja retirada é amanhã.
     */
    @Scheduled(cron = "${app.notificacao.lembrete.cron:0 0 9 * * *}")
    public void lembreteRetirada() {
        LocalDate amanha = LocalDate.now().plusDays(1);
        List<Locacao> locacoes = locacaoPort.buscarReservadosComRetiradaEm(amanha);
        log.info("LembreteRetirada: {} locacao(es) com retirada em {}", locacoes.size(), amanha);

        for (Locacao locacao : locacoes) {
            try {
                contextBuilder.construir(locacao).ifPresent(r ->
                    notificacaoService.enviar(
                        TipoNotificacao.LEMBRETE_RETIRADA,
                        r.email(),
                        r.tenantId(),
                        r.contexto()
                    )
                );
            } catch (Exception e) {
                log.error("Erro ao enviar lembrete de retirada para locacaoId={}: {}", locacao.getId(), e.getMessage());
            }
        }
    }

    /**
     * Lembrete de devolução: dispara para todas as locações retiradas cuja devolução é hoje.
     */
    @Scheduled(cron = "${app.notificacao.lembrete.cron:0 0 9 * * *}")
    public void lembreteDevolucao() {
        LocalDate hoje = LocalDate.now();
        List<Locacao> locacoes = locacaoPort.buscarRetiradosComDevolucaoEm(hoje);
        log.info("LembreteDevolucao: {} locacao(es) com devolucao em {}", locacoes.size(), hoje);

        for (Locacao locacao : locacoes) {
            try {
                contextBuilder.construir(locacao).ifPresent(r ->
                    notificacaoService.enviar(
                        TipoNotificacao.LEMBRETE_DEVOLUCAO,
                        r.email(),
                        r.tenantId(),
                        r.contexto()
                    )
                );
            } catch (Exception e) {
                log.error("Erro ao enviar lembrete de devolucao para locacaoId={}: {}", locacao.getId(), e.getMessage());
            }
        }
    }

    /**
     * Aviso de atraso: dispara para todas as locações retiradas com dataFim anterior a hoje.
     * Calcula dias de atraso e multa estimada no contexto.
     */
    @Scheduled(cron = "${app.notificacao.lembrete.cron:0 0 9 * * *}")
    public void avisoAtraso() {
        LocalDate hoje = LocalDate.now();
        List<Locacao> locacoes = locacaoPort.buscarRetiradosEmAtraso(hoje);
        log.info("AvisoAtraso: {} locacao(es) em atraso", locacoes.size());

        for (Locacao locacao : locacoes) {
            try {
                long diasAtraso = ChronoUnit.DAYS.between(locacao.getDataFim(), hoje);
                BigDecimal multaEstimada = calcularMultaEstimada(locacao, diasAtraso);

                contextBuilder.construir(locacao).ifPresent(r -> {
                    Map<String, Object> ctx = new HashMap<>(r.contexto());
                    ctx.put("diasAtraso",  String.valueOf(diasAtraso));
                    ctx.put("multaAtraso", multaEstimada.toPlainString());

                    notificacaoService.enviar(
                        TipoNotificacao.AVISO_ATRASO,
                        r.email(),
                        r.tenantId(),
                        ctx
                    );
                });
            } catch (Exception e) {
                log.error("Erro ao enviar aviso de atraso para locacaoId={}: {}", locacao.getId(), e.getMessage());
            }
        }
    }

    /**
     * Calcula a multa estimada por atraso: soma dos subtotais dos itens * percentual * diasAtraso.
     * Usa a multa já registrada na locação ou estima com percentual padrão (10% ao dia).
     */
    private BigDecimal calcularMultaEstimada(Locacao locacao, long diasAtraso) {
        if (locacao.getMulta() != null && locacao.getMulta().compareTo(BigDecimal.ZERO) > 0) {
            return locacao.getMulta();
        }
        if (locacao.getItens() == null || locacao.getItens().isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalDiarias = locacao.getItens().stream()
                .map(item -> item.getValorDiaria() != null ? item.getValorDiaria() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // 10% ao dia sobre o valor das diárias
        return totalDiarias.multiply(new BigDecimal("0.10")).multiply(BigDecimal.valueOf(diasAtraso));
    }
}
