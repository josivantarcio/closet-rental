package com.jtarcio.closetrental.modules.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepositoryJpa extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.username = :username")
    void deleteByUsername(@Param("username") String username);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.expiraEm < :agora")
    void deleteExpired(@Param("agora") LocalDateTime agora);
}
