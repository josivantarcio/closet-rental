package com.jtarcio.closetrental.modules.notificacoes.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepositoryJpa extends JpaRepository<NotificacaoEntity, Long> {
}
