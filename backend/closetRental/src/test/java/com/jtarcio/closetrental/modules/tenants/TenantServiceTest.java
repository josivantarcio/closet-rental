package com.jtarcio.closetrental.modules.tenants;

import com.jtarcio.closetrental.modules.tenants.application.TenantService;
import com.jtarcio.closetrental.modules.tenants.domain.model.Tenant;
import com.jtarcio.closetrental.modules.tenants.domain.ports.TenantRepositoryPort;
import com.jtarcio.closetrental.shared.exception.ConflictException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantService")
class TenantServiceTest {

    @Mock
    private TenantRepositoryPort repositoryPort;

    @InjectMocks
    private TenantService tenantService;

    private Tenant tenantValido;

    @BeforeEach
    void setUp() {
        tenantValido = new Tenant(
                UUID.randomUUID(), "Loja Teste", "loja-teste", "FREE",
                true, LocalDateTime.now(), null
        );
    }

    @Nested
    @DisplayName("criar()")
    class Criar {

        @Test
        @DisplayName("deve criar tenant quando slug não existir")
        void deveCriarComSucesso() {
            when(repositoryPort.existePorSlug("loja-teste")).thenReturn(false);
            when(repositoryPort.salvar(any())).thenReturn(tenantValido);

            Tenant criado = tenantService.criar(tenantValido);

            assertThat(criado).isNotNull();
            assertThat(criado.getSlug()).isEqualTo("loja-teste");
            verify(repositoryPort).salvar(tenantValido);
        }

        @Test
        @DisplayName("deve lançar ConflictException quando slug já existir")
        void deveLancarConflictQuandoSlugJaExiste() {
            when(repositoryPort.existePorSlug("loja-teste")).thenReturn(true);

            assertThatThrownBy(() -> tenantService.criar(tenantValido))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("loja-teste");

            verify(repositoryPort, never()).salvar(any());
        }
    }

    @Nested
    @DisplayName("ativar() / desativar()")
    class AtivarDesativar {

        @Test
        @DisplayName("deve ativar tenant inativo")
        void deveAtivar() {
            tenantValido.setAtivo(false);
            when(repositoryPort.buscarPorId(tenantValido.getId())).thenReturn(Optional.of(tenantValido));
            when(repositoryPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Tenant resultado = tenantService.ativar(tenantValido.getId());

            assertThat(resultado.isAtivo()).isTrue();
        }

        @Test
        @DisplayName("deve desativar tenant ativo")
        void deveDesativar() {
            when(repositoryPort.buscarPorId(tenantValido.getId())).thenReturn(Optional.of(tenantValido));
            when(repositoryPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Tenant resultado = tenantService.desativar(tenantValido.getId());

            assertThat(resultado.isAtivo()).isFalse();
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException para ID inexistente")
        void deveLancarNotFoundQuandoIdInexistente() {
            UUID idInexistente = UUID.randomUUID();
            when(repositoryPort.buscarPorId(idInexistente)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tenantService.ativar(idInexistente))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
