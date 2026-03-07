package com.jtarcio.closetrental.modules.notificacoes.infrastructure.persistence;

import com.jtarcio.closetrental.modules.notificacoes.domain.model.StatusNotificacao;
import com.jtarcio.closetrental.modules.notificacoes.domain.model.TipoNotificacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "notificacoes",
    indexes = {
        @Index(name = "idx_notificacoes_tenant",  columnList = "tenant_id"),
        @Index(name = "idx_notificacoes_tipo",    columnList = "tipo"),
        @Index(name = "idx_notificacoes_status",  columnList = "status"),
        @Index(name = "idx_notificacoes_enviado", columnList = "enviado_em")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoNotificacao tipo;

    @Column(nullable = false, length = 20)
    private String canal;

    @Column(nullable = false, length = 255)
    private String destinatario;

    @Column(nullable = false, length = 255)
    private String assunto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusNotificacao status;

    @Column(columnDefinition = "TEXT")
    private String erro;

    @Column(name = "enviado_em", nullable = false)
    private LocalDateTime enviadoEm;
}
