package com.jtarcio.closetrental.modules.notificacoes.application;

import com.jtarcio.closetrental.modules.notificacoes.domain.model.TipoNotificacao;

import java.util.Map;
import java.util.UUID;

/**
 * Porta de saída para envio de notificações.
 * Implementações: email (síncrono), WhatsApp (futuro — assíncrono com fila).
 */
public interface NotificacaoService {

    /**
     * Envia uma notificação ao destinatário.
     *
     * @param tipo         tipo do evento que originou a notificação
     * @param email        endereço de e-mail do destinatário
     * @param tenantId     tenant associado (para log; pode ser null para SUPER_ADMIN)
     * @param contexto     dados dinâmicos para montar o corpo da mensagem
     */
    void enviar(TipoNotificacao tipo, String email, UUID tenantId, Map<String, Object> contexto);
}
