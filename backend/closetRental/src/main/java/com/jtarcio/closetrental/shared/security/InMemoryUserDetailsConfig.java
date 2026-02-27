package com.jtarcio.closetrental.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Configuração de usuários in-memory separada do SecurityConfig para evitar
 * dependência circular: SecurityConfig → JwtAuthenticationFilter → UserDetailsService → SecurityConfig.
 *
 * Usuários disponíveis:
 * - admin / admin123       → ROLE_ADMIN
 * - atendente / atendente123 → ROLE_ATENDENTE
 * - cliente / cliente123   → ROLE_CLIENTE
 */
@Configuration
public class InMemoryUserDetailsConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        UserDetails atendente = User.builder()
                .username("atendente")
                .password(encoder.encode("atendente123"))
                .roles("ATENDENTE")
                .build();

        UserDetails cliente = User.builder()
                .username("cliente")
                .password(encoder.encode("cliente123"))
                .roles("CLIENTE")
                .build();

        return new InMemoryUserDetailsManager(admin, atendente, cliente);
    }
}
