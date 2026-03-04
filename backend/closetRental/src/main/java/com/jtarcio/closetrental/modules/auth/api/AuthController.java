package com.jtarcio.closetrental.modules.auth.api;

import com.jtarcio.closetrental.modules.auth.api.dto.LoginRequest;
import com.jtarcio.closetrental.modules.auth.api.dto.LoginResponse;
import com.jtarcio.closetrental.modules.auth.api.dto.RefreshRequest;
import com.jtarcio.closetrental.modules.auth.application.RefreshTokenService;
import com.jtarcio.closetrental.modules.auth.infrastructure.persistence.RefreshTokenEntity;
import com.jtarcio.closetrental.shared.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller de autenticação.
 *
 * POST /v1/auth/login   — retorna access token (JWT) + refresh token (opaco)
 * POST /v1/auth/refresh — usa refresh token para emitir novo access token
 * POST /v1/auth/logout  — revoga o refresh token do usuário
 */
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login, refresh e logout com JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;

    @Value("${app.jwt.expiration}")
    private long expirationSeconds;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário",
               description = "Login com username/password. Retorna access token (JWT) e refresh token.")
    @ApiResponse(responseCode = "200", description = "Tokens gerados com sucesso")
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.gerarToken(userDetails);
        String refreshToken = refreshTokenService.criarRefreshToken(userDetails.getUsername());

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new LoginResponse(accessToken, "Bearer", expirationSeconds,
                userDetails.getUsername(), roles, refreshToken);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token",
               description = "Usa o refresh token para emitir um novo access token JWT.")
    @ApiResponse(responseCode = "200", description = "Novo access token emitido")
    @ApiResponse(responseCode = "400", description = "Refresh token inválido ou expirado")
    public LoginResponse refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshTokenEntity entity = refreshTokenService.validar(request.refreshToken());

        UserDetails userDetails = userDetailsService.loadUserByUsername(entity.getUsername());
        String novoAccessToken = jwtService.gerarToken(userDetails);
        String novoRefreshToken = refreshTokenService.criarRefreshToken(entity.getUsername());

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new LoginResponse(novoAccessToken, "Bearer", expirationSeconds,
                entity.getUsername(), roles, novoRefreshToken);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoga o refresh token do usuário autenticado.")
    @ApiResponse(responseCode = "204", description = "Logout realizado")
    public void logout(@Valid @RequestBody RefreshRequest request) {
        RefreshTokenEntity entity = refreshTokenService.validar(request.refreshToken());
        refreshTokenService.revogarPorUsername(entity.getUsername());
    }
}
