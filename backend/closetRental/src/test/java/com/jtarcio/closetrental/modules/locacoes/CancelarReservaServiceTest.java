package com.jtarcio.closetrental.modules.locacoes;

import com.jtarcio.closetrental.modules.locacoes.application.CancelarReservaService;
import com.jtarcio.closetrental.modules.locacoes.domain.model.ItemLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.modules.notificacoes.application.NotificacaoContextBuilder;
import com.jtarcio.closetrental.modules.notificacoes.application.NotificacaoService;
import com.jtarcio.closetrental.modules.tenants.domain.model.Tenant;
import com.jtarcio.closetrental.modules.tenants.domain.ports.TenantRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelarReservaService")
class CancelarReservaServiceTest {

    @Mock private LocacaoRepositoryPort locacaoPort;
    @Mock private TenantRepositoryPort tenantPort;
    @Mock private NotificacaoService notificacaoService;
    @Mock private NotificacaoContextBuilder contextBuilder;

    @InjectMocks
    private CancelarReservaService service;

    private Locacao locacaoReservada;
    private final LocalDate HOJE = LocalDate.now();

    @BeforeEach
    void setUp() {
        locacaoReservada = new Locacao();
        locacaoReservada.setId(1L);
        locacaoReservada.setStatus(StatusLocacao.RESERVADO);
        // datas para teste de multa: inicio em 7+ dias → isento por default
        locacaoReservada.setDataInicio(HOJE.plusDays(10));
        locacaoReservada.setDataFim(HOJE.plusDays(12));

        ItemLocacao item = new ItemLocacao();
        item.setValorDiaria(new BigDecimal("100.00"));
        locacaoReservada.setItens(List.of(item));

        lenient().when(contextBuilder.construir(any())).thenReturn(java.util.Optional.empty());
    }

    @Nested
    @DisplayName("cancelamento bem-sucedido")
    class CancelamentoSucesso {

        @Test
        @DisplayName("deve cancelar reserva no status RESERVADO")
        void deveCancelarReservada() {
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoReservada));
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Locacao resultado = service.executar(1L, "Cliente desistiu");

            assertThat(resultado.getStatus()).isEqualTo(StatusLocacao.CANCELADO);
            assertThat(resultado.getObservacoes()).isEqualTo("Cliente desistiu");
        }

        @Test
        @DisplayName("deve cancelar sem motivo quando motivo e null")
        void deveCancelarSemMotivo() {
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoReservada));
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Locacao resultado = service.executar(1L, null);

            assertThat(resultado.getStatus()).isEqualTo(StatusLocacao.CANCELADO);
        }

        @Test
        @DisplayName("deve aplicar multa ZERO quando inicio e em 7+ dias (politica default)")
        void deveAplicarMultaZeroIsentoDefault() {
            // dataInicio = HOJE + 10 dias → isento
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoReservada));
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Locacao resultado = service.executar(1L, null);

            assertThat(resultado.getMultaCancelamento()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("deve aplicar multa 30% quando inicio e em 3-6 dias (politica default sem tenant)")
        void deveAplicarMulta30PorCentoSemTenant() {
            // 3 dias * R$100/dia = R$300 base; 30% = R$90
            locacaoReservada.setDataInicio(HOJE.plusDays(5));
            locacaoReservada.setDataFim(HOJE.plusDays(7));
            locacaoReservada.setTenantId(null);

            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoReservada));
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Locacao resultado = service.executar(1L, null);

            // base = 100 * 3 dias = 300; 30% de 300 = 90
            assertThat(resultado.getMultaCancelamento()).isEqualByComparingTo("90.00");
        }

        @Test
        @DisplayName("deve usar politica customizada do tenant quando encontrado")
        void deveUsarPoliticaCustomizadaDeTenant() {
            UUID tenantId = UUID.randomUUID();
            locacaoReservada.setTenantId(tenantId);
            locacaoReservada.setDataInicio(HOJE.plusDays(5));
            locacaoReservada.setDataFim(HOJE.plusDays(7));

            Tenant tenantCustom = new Tenant();
            tenantCustom.setPctCancelamento3d(new BigDecimal("50.00")); // 50% no lugar de 30%

            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoReservada));
            when(tenantPort.buscarPorId(tenantId)).thenReturn(Optional.of(tenantCustom));
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Locacao resultado = service.executar(1L, null);

            // base = 100 * 3 dias = 300; 50% de 300 = 150
            assertThat(resultado.getMultaCancelamento()).isEqualByComparingTo("150.00");
        }

        @Test
        @DisplayName("deve usar politica default quando tenant nao encontrado")
        void deveUsarPoliticaDefaultQuandoTenantNaoEncontrado() {
            UUID tenantId = UUID.randomUUID();
            locacaoReservada.setTenantId(tenantId);
            locacaoReservada.setDataInicio(HOJE.plusDays(5));
            locacaoReservada.setDataFim(HOJE.plusDays(7));

            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoReservada));
            when(tenantPort.buscarPorId(tenantId)).thenReturn(Optional.empty()); // tenant não encontrado
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Locacao resultado = service.executar(1L, null);

            // usa default 30%; base = 300; multa = 90
            assertThat(resultado.getMultaCancelamento()).isEqualByComparingTo("90.00");
        }
    }

    @Nested
    @DisplayName("cancelamento com status invalido")
    class CancelamentoStatusInvalido {

        @Test
        @DisplayName("deve lancar BusinessException ao cancelar locacao RETIRADA")
        void deveLancarExcecaoSeRetirada() {
            locacaoReservada.setStatus(StatusLocacao.RETIRADO);
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoReservada));

            assertThatThrownBy(() -> service.executar(1L, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("RESERVADO");
        }

        @Test
        @DisplayName("deve lancar BusinessException ao cancelar locacao DEVOLVIDA")
        void deveLancarExcecaoSeDevolvida() {
            locacaoReservada.setStatus(StatusLocacao.DEVOLVIDO);
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoReservada));

            assertThatThrownBy(() -> service.executar(1L, null))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("deve lancar BusinessException ao cancelar locacao ja CANCELADA")
        void deveLancarExcecaoSeCancelada() {
            locacaoReservada.setStatus(StatusLocacao.CANCELADO);
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoReservada));

            assertThatThrownBy(() -> service.executar(1L, null))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Test
    @DisplayName("deve lancar EntityNotFoundException quando locacao nao existe")
    void deveLancarEntityNotFound() {
        when(locacaoPort.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.executar(99L, null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }
}
