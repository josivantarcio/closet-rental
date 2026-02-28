package com.jtarcio.closetrental.shared.config;

import com.jtarcio.closetrental.shared.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuração de segurança JWT stateless.
 *
 * Fluxo:
 * 1. POST /v1/auth/login → retorna Bearer token
 * 2. Demais endpoints exigem header: Authorization: Bearer <token>
 * 3. JwtAuthenticationFilter valida o token e popula o SecurityContext
 * 4. Sessão HTTP desabilitada (STATELESS)
 *
 * UserDetailsService e PasswordEncoder definidos em InMemoryUserDetailsConfig
 * para evitar dependência circular com JwtAuthenticationFilter.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Login público
                        .requestMatchers(HttpMethod.POST, "/v1/auth/login").permitAll()

                        // Swagger e docs públicos
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        // Catálogo — leitura pública, escrita restrita
                        .requestMatchers(HttpMethod.GET, "/v1/catalogo/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/catalogo/**").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.PUT, "/v1/catalogo/**").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.DELETE, "/v1/catalogo/**").hasRole("ADMIN")

                        // Clientes
                        .requestMatchers("/v1/clientes/**").hasAnyRole("ADMIN", "ATENDENTE")

                        // Locações
                        .requestMatchers("/v1/locacoes/**").hasAnyRole("ADMIN", "ATENDENTE", "CLIENTE")

                        // Estoque
                        .requestMatchers("/v1/estoque/**").hasAnyRole("ADMIN", "ATENDENTE")

                        // Pagamentos — estorno apenas ADMIN
                        .requestMatchers(HttpMethod.POST, "/v1/pagamentos/*/estornar").hasRole("ADMIN")
                        .requestMatchers("/v1/pagamentos/**").hasAnyRole("ADMIN", "ATENDENTE", "CLIENTE")

                        // Relatórios
                        .requestMatchers("/v1/relatorios/**").hasAnyRole("ADMIN", "ATENDENTE")

                        // Qualquer outra rota exige autenticação
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(401, "Nao autenticado")))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
