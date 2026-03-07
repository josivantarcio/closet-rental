package com.jtarcio.closetrental.modules.relatorios;

import com.jtarcio.closetrental.modules.relatorios.api.dto.AtrasosPendentesResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.DisponibilidadeDataResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.FaturamentoResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.HistoricoClienteResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.PecaMaisAlugadaResponse;
import com.jtarcio.closetrental.modules.relatorios.application.RelatorioService;
import com.jtarcio.closetrental.modules.relatorios.domain.ports.RelatorioRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RelatorioService")
class RelatorioServiceTest {

    @Mock RelatorioRepositoryPort relatorioPort;
    @InjectMocks RelatorioService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "percentualMultaDiaria", new BigDecimal("0.10"));
    }

    private final Pageable pageable = PageRequest.of(0, 10);

    // ─────────────────────────── disponibilidade ───────────────────────────

    @Nested
    @DisplayName("disponibilidadeNaData()")
    class Disponibilidade {

        @Test
        @DisplayName("retorna página de produtos disponíveis na data")
        void retornaDisponiveisNaData() {
            LocalDate data = LocalDate.of(2026, 3, 15);
            DisponibilidadeDataResponse dto = new DisponibilidadeDataResponse(1L, "Vestido", "VESTIDO", new BigDecimal("85.00"));
            when(relatorioPort.disponibilidadeNaData(eq(data), any())).thenReturn(new PageImpl<>(List.of(dto)));

            Page<DisponibilidadeDataResponse> result = service.disponibilidadeNaData(data, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).nome()).isEqualTo("Vestido");
        }

        @Test
        @DisplayName("retorna página vazia quando nenhuma peça disponível")
        void retornaVazioSemPecas() {
            when(relatorioPort.disponibilidadeNaData(any(), any())).thenReturn(Page.empty());

            Page<DisponibilidadeDataResponse> result = service.disponibilidadeNaData(LocalDate.now(), pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    // ─────────────────────────── histórico cliente ───────────────────────────

    @Nested
    @DisplayName("historicoCliente()")
    class HistoricoCliente {

        @Test
        @DisplayName("retorna histórico paginado do cliente")
        void retornaHistorico() {
            HistoricoClienteResponse dto = new HistoricoClienteResponse(
                    10L, "DEVOLVIDO",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3),
                    new BigDecimal("270.00"), null, LocalDateTime.now());
            when(relatorioPort.historicoCliente(eq(5L), any())).thenReturn(new PageImpl<>(List.of(dto)));

            Page<HistoricoClienteResponse> result = service.historicoCliente(5L, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).locacaoId()).isEqualTo(10L);
        }
    }

    // ─────────────────────────── faturamento ───────────────────────────

    @Nested
    @DisplayName("faturamento()")
    class Faturamento {

        @Test
        @DisplayName("retorna faturamento do mês informado")
        void retornaFaturamento() {
            FaturamentoResponse expected = new FaturamentoResponse(3, 2026, new BigDecimal("4200.00"), 12L);
            when(relatorioPort.faturamento(3, 2026)).thenReturn(expected);

            FaturamentoResponse result = service.faturamento(3, 2026);

            assertThat(result.totalReceita()).isEqualByComparingTo("4200.00");
            assertThat(result.totalPagamentos()).isEqualTo(12L);
        }

        @Test
        @DisplayName("mes inválido lança BusinessException")
        void mesInvalidoLancaExcecao() {
            assertThatThrownBy(() -> service.faturamento(13, 2026))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Mês inválido");
        }

        @Test
        @DisplayName("ano inválido lança BusinessException")
        void anoInvalidoLancaExcecao() {
            assertThatThrownBy(() -> service.faturamento(1, 1999))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Ano inválido");
        }
    }

    // ─────────────────────────── peças mais alugadas ───────────────────────────

    @Nested
    @DisplayName("pecasMaisAlugadas()")
    class PecasMaisAlugadas {

        @Test
        @DisplayName("retorna ranking com limite informado")
        void retornaRanking() {
            PecaMaisAlugadaResponse dto = new PecaMaisAlugadaResponse(1L, "Vestido Longo", "VESTIDO", 15L);
            when(relatorioPort.pecasMaisAlugadas(5)).thenReturn(List.of(dto));

            List<PecaMaisAlugadaResponse> result = service.pecasMaisAlugadas(5);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).totalLocacoes()).isEqualTo(15L);
        }

        @Test
        @DisplayName("limite acima de 100 lança BusinessException")
        void limiteAcimaMaxLancaExcecao() {
            assertThatThrownBy(() -> service.pecasMaisAlugadas(101))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Limite inválido");
        }
    }

    // ─────────────────────────── atrasos pendentes ───────────────────────────

    @Nested
    @DisplayName("atrasosPendentes()")
    class AtrasosPendentes {

        @Test
        @DisplayName("retorna atrasos com diasAtraso e multaEstimada calculados")
        void retornaAtrasosComMulta() {
            LocalDate dataFim = LocalDate.now().minusDays(3);
            // A query retorna diasAtraso=0 e multaEstimada=valorDiaria (placeholder)
            // O service recalcula com o percentual configurado
            AtrasosPendentesResponse raw = new AtrasosPendentesResponse(
                    1L, 2L, 3L, dataFim, 0L, new BigDecimal("100.00"), new BigDecimal("100.00"));
            when(relatorioPort.atrasosPendentes(any(), any())).thenReturn(new PageImpl<>(List.of(raw)));

            Page<AtrasosPendentesResponse> result = service.atrasosPendentes(pageable);

            assertThat(result.getContent()).hasSize(1);
            AtrasosPendentesResponse r = result.getContent().get(0);
            assertThat(r.diasAtraso()).isEqualTo(3L);
            // multa = 100 * 3 dias * 0.10 = 30.00
            assertThat(r.multaEstimada()).isEqualByComparingTo("30.00");
        }
    }
}
