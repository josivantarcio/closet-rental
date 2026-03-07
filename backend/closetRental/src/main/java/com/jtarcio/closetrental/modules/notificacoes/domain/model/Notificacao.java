package com.jtarcio.closetrental.modules.notificacoes.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro de uma notificação enviada (ou que falhou).
 * Serve como log de auditoria dos envios do sistema.
 */
public class Notificacao {

    private Long id;
    private UUID tenantId;
    private TipoNotificacao tipo;
    private String canal;       // "EMAIL"
    private String destinatario;
    private String assunto;
    private StatusNotificacao status;
    private String erro;
    private LocalDateTime enviadoEm;

    public Notificacao() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public TipoNotificacao getTipo() { return tipo; }
    public void setTipo(TipoNotificacao tipo) { this.tipo = tipo; }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }

    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    public String getAssunto() { return assunto; }
    public void setAssunto(String assunto) { this.assunto = assunto; }

    public StatusNotificacao getStatus() { return status; }
    public void setStatus(StatusNotificacao status) { this.status = status; }

    public String getErro() { return erro; }
    public void setErro(String erro) { this.erro = erro; }

    public LocalDateTime getEnviadoEm() { return enviadoEm; }
    public void setEnviadoEm(LocalDateTime enviadoEm) { this.enviadoEm = enviadoEm; }
}
