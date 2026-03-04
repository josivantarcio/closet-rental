package com.jtarcio.closetrental.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuração do PasswordEncoder separada do SecurityConfig para evitar
 * dependência circular: SecurityConfig → JwtAuthenticationFilter → UserDetailsService → SecurityConfig.
 *
 * UserDetailsService agora é implementado por UserDetailsServiceImpl
 * que carrega usuários do banco de dados (tabela `usuarios`).
 */
@Configuration
public class InMemoryUserDetailsConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
