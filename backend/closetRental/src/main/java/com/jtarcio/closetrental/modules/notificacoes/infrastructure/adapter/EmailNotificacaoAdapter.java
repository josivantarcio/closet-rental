package com.jtarcio.closetrental.modules.notificacoes.infrastructure.adapter;

import com.jtarcio.closetrental.modules.notificacoes.application.NotificacaoService;
import com.jtarcio.closetrental.modules.notificacoes.domain.model.Notificacao;
import com.jtarcio.closetrental.modules.notificacoes.domain.model.StatusNotificacao;
import com.jtarcio.closetrental.modules.notificacoes.domain.model.TipoNotificacao;
import com.jtarcio.closetrental.modules.notificacoes.infrastructure.persistence.NotificacaoEntity;
import com.jtarcio.closetrental.modules.notificacoes.infrastructure.persistence.NotificacaoRepositoryJpa;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Implementação de {@link NotificacaoService} via SMTP (Spring Mail).
 *
 * Quando {@code app.notificacao.email.enabled=false} (padrão em dev/test),
 * o envio é pulado mas o log é mantido para diagnóstico.
 *
 * Cada envio é persistido em {@code notificacoes} com status ENVIADA ou FALHA.
 * Erros de email nunca propagam exceção — são absorvidos e logados (fire-and-log).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificacaoAdapter implements NotificacaoService {

    private final JavaMailSender mailSender;
    private final NotificacaoRepositoryJpa repositorio;

    @Value("${app.notificacao.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${app.notificacao.email.remetente:noreply@closetrental.com.br}")
    private String remetente;

    @Value("${app.notificacao.email.nome-remetente:Closet Rental}")
    private String nomeRemetente;

    @Override
    public void enviar(TipoNotificacao tipo, String email, UUID tenantId, Map<String, Object> contexto) {
        if (email == null || email.isBlank()) {
            log.warn("Notificacao {} ignorada — email do destinatario nulo ou vazio", tipo);
            return;
        }

        String assunto = resolverAssunto(tipo, contexto);
        String corpo   = resolverCorpo(tipo, contexto);

        StatusNotificacao status = StatusNotificacao.ENVIADA;
        String erro = null;

        if (emailEnabled) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(nomeRemetente + " <" + remetente + ">");
                msg.setTo(email);
                msg.setSubject(assunto);
                msg.setText(corpo);
                mailSender.send(msg);
                log.info("Email enviado: tipo={}, destinatario={}", tipo, email);
            } catch (MailException e) {
                status = StatusNotificacao.FALHA;
                erro = e.getMessage();
                log.error("Falha ao enviar email: tipo={}, destinatario={}, erro={}", tipo, email, e.getMessage());
            }
        } else {
            log.debug("Email desabilitado (MAIL_ENABLED=false). Simulando envio: tipo={}, destinatario={}", tipo, email);
        }

        registrar(tipo, email, tenantId, assunto, status, erro);
    }

    // ── Templates de assunto e corpo ─────────────────────────────────────────

    private String resolverAssunto(TipoNotificacao tipo, Map<String, Object> ctx) {
        return switch (tipo) {
            case RESERVA_CRIADA        -> "Reserva confirmada — " + ctx.getOrDefault("tenantNome", "Atelier");
            case RETIRADA_REGISTRADA   -> "Retirada registrada — devolução em " + ctx.getOrDefault("dataFim", "");
            case DEVOLUCAO_REGISTRADA  -> "Devolução confirmada — obrigado!";
            case CANCELAMENTO_RESERVA  -> "Reserva cancelada — " + ctx.getOrDefault("tenantNome", "Atelier");
        };
    }

    private String resolverCorpo(TipoNotificacao tipo, Map<String, Object> ctx) {
        String clienteNome  = str(ctx, "clienteNome");
        String tenantNome   = str(ctx, "tenantNome");
        String locacaoId    = str(ctx, "locacaoId");
        String dataInicio   = str(ctx, "dataInicio");
        String dataFim      = str(ctx, "dataFim");
        String valorTotal   = str(ctx, "valorTotal");

        return switch (tipo) {
            case RESERVA_CRIADA -> """
                    Olá, %s!

                    Sua reserva foi confirmada com sucesso.

                    Detalhes:
                    - Contrato nº: %s
                    - Retirada:    %s
                    - Devolução:   %s
                    - Valor total: R$ %s

                    Em caso de dúvidas, entre em contato com %s.

                    Até logo!
                    """.formatted(clienteNome, locacaoId, dataInicio, dataFim, valorTotal, tenantNome);

            case RETIRADA_REGISTRADA -> """
                    Olá, %s!

                    A retirada das peças do contrato nº %s foi registrada.

                    Lembre-se de devolver até %s.

                    Qualquer dúvida, fale com %s.
                    """.formatted(clienteNome, locacaoId, dataFim, tenantNome);

            case DEVOLUCAO_REGISTRADA -> {
                String multa = str(ctx, "multa");
                String caucaoStatus = str(ctx, "statusCaucao");
                yield """
                    Olá, %s!

                    A devolução do contrato nº %s foi registrada com sucesso.

                    %s%sObrigado por usar %s. Esperamos vê-lo(a) em breve!
                    """.formatted(
                        clienteNome, locacaoId,
                        multa.isBlank() || multa.equals("0") ? "" : "Multa por atraso: R$ " + multa + "\n",
                        caucaoStatus.isBlank() ? "" : "Status da caução: " + caucaoStatus + "\n",
                        tenantNome
                    );
            }

            case CANCELAMENTO_RESERVA -> {
                String multaCancelamento = str(ctx, "multaCancelamento");
                yield """
                    Olá, %s!

                    Sua reserva nº %s foi cancelada.

                    %sEm caso de dúvidas, entre em contato com %s.
                    """.formatted(
                        clienteNome, locacaoId,
                        multaCancelamento.isBlank() || multaCancelamento.equals("0")
                            ? ""
                            : "Multa de cancelamento aplicada: R$ " + multaCancelamento + "\n\n",
                        tenantNome
                    );
            }
        };
    }

    // ── Persistência do log ───────────────────────────────────────────────────

    private void registrar(TipoNotificacao tipo, String email, UUID tenantId,
                           String assunto, StatusNotificacao status, String erro) {
        try {
            NotificacaoEntity entity = NotificacaoEntity.builder()
                    .tenantId(tenantId)
                    .tipo(tipo)
                    .canal("EMAIL")
                    .destinatario(email)
                    .assunto(assunto)
                    .status(status)
                    .erro(erro)
                    .enviadoEm(LocalDateTime.now())
                    .build();
            repositorio.save(entity);
        } catch (Exception e) {
            // Log de notificação nunca deve derrubar o fluxo principal
            log.error("Falha ao persistir log de notificacao: {}", e.getMessage());
        }
    }

    private String str(Map<String, Object> ctx, String chave) {
        Object val = ctx.get(chave);
        return val != null ? val.toString() : "";
    }
}
