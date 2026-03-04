package com.jtarcio.closetrental.modules.auth.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36, unique = true)
    private String token;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private boolean revogado;

    @PrePersist
    void prePersist() {
        criadoEm = LocalDateTime.now();
    }

    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(expiraEm);
    }
}
