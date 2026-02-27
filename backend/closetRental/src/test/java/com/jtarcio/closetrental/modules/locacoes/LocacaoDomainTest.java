package com.jtarcio.closetrental.modules.locacoes;

import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Locacao domain model")
class LocacaoDomainTest {

    private Locacao locacao;

    @BeforeEach
    void setUp() {
        locacao = new Locacao();
        locacao.setValorDiaria(new BigDecimal("100.00"));
        locacao.setCaucao(new BigDecimal("150.00"));
        locacao.setMulta(BigDecimal.ZERO);
    }

    @Nested
    @DisplayName("calcularDias()")
    class CalcularDias {

        @Test
        @DisplayName("deve calcular 1 dia quando inicio e fim diferem por 1 dia")
        void umDia() {
            locacao.setDataInicio(LocalDate.of(2026, 3, 10));
            locacao.setDataFim(LocalDate.of(2026, 3, 11));
            assertThat(locacao.calcularDias()).isEqualTo(2); // inclusivo
        }

        @Test
        @DisplayName("deve calcular 7 dias para uma semana completa")
        void seteDias() {
            locacao.setDataInicio(LocalDate.of(2026, 3, 10));
            locacao.setDataFim(LocalDate.of(2026, 3, 16));
            assertThat(locacao.calcularDias()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("calcularValorTotal()")
    class CalcularValorTotal {

        @Test
        @DisplayName("deve calcular valorTotal = diaria * dias + caucao")
        void calcularValorTotal() {
            locacao.setDataInicio(LocalDate.of(2026, 3, 10));
            locacao.setDataFim(LocalDate.of(2026, 3, 12)); // 3 dias
            // 100 * 3 + 150 = 450
            assertThat(locacao.calcularValorTotal()).isEqualByComparingTo("450.00");
        }

        @Test
        @DisplayName("deve calcular valorTotal sem caucao (null tratado como zero)")
        void calcularSemCaucao() {
            locacao.setCaucao(null);
            locacao.setDataInicio(LocalDate.of(2026, 3, 10));
            locacao.setDataFim(LocalDate.of(2026, 3, 12)); // 3 dias
            // 100 * 3 + 0 = 300
            assertThat(locacao.calcularValorTotal()).isEqualByComparingTo("300.00");
        }
    }

    @Nested
    @DisplayName("calcularMulta()")
    class CalcularMulta {

        @Test
        @DisplayName("deve retornar ZERO quando nao ha atraso (devolvido antes do prazo)")
        void semAtraso() {
            locacao.setDataFim(LocalDate.now().plusDays(1)); // ainda no prazo
            BigDecimal multa = locacao.calcularMulta(LocalDateTime.now(), new BigDecimal("0.10"));
            assertThat(multa).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("deve retornar ZERO quando devolvido no dia exato do fim")
        void devolucaoNoDiaExato() {
            locacao.setDataFim(LocalDate.now());
            BigDecimal multa = locacao.calcularMulta(LocalDateTime.now(), new BigDecimal("0.10"));
            assertThat(multa).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("deve calcular multa correta com 1 dia de atraso: 10% * 100 * 1 = 10")
        void umDiaAtraso() {
            locacao.setDataFim(LocalDate.now().minusDays(1));
            BigDecimal multa = locacao.calcularMulta(LocalDateTime.now(), new BigDecimal("0.10"));
            assertThat(multa).isEqualByComparingTo("10.00");
        }

        @Test
        @DisplayName("deve calcular multa com 5 dias de atraso: 10% * 100 * 5 = 50")
        void cincoDiasAtraso() {
            locacao.setDataFim(LocalDate.now().minusDays(5));
            BigDecimal multa = locacao.calcularMulta(LocalDateTime.now(), new BigDecimal("0.10"));
            assertThat(multa).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("deve retornar ZERO quando devolucaoReal e null")
        void devolucaoNula() {
            locacao.setDataFim(LocalDate.now().minusDays(3));
            BigDecimal multa = locacao.calcularMulta(null, new BigDecimal("0.10"));
            assertThat(multa).isEqualByComparingTo("0.00");
        }
    }

    @Nested
    @DisplayName("Transicoes de estado")
    class TransicoesEstado {

        @Test
        @DisplayName("podeRetirar() = true apenas quando RESERVADO")
        void podeRetirar() {
            locacao.setStatus(StatusLocacao.RESERVADO);
            assertThat(locacao.podeRetirar()).isTrue();

            locacao.setStatus(StatusLocacao.RETIRADO);
            assertThat(locacao.podeRetirar()).isFalse();

            locacao.setStatus(StatusLocacao.DEVOLVIDO);
            assertThat(locacao.podeRetirar()).isFalse();

            locacao.setStatus(StatusLocacao.CANCELADO);
            assertThat(locacao.podeRetirar()).isFalse();
        }

        @Test
        @DisplayName("podeDevolver() = true apenas quando RETIRADO")
        void podeDevolver() {
            locacao.setStatus(StatusLocacao.RETIRADO);
            assertThat(locacao.podeDevolver()).isTrue();

            locacao.setStatus(StatusLocacao.RESERVADO);
            assertThat(locacao.podeDevolver()).isFalse();

            locacao.setStatus(StatusLocacao.DEVOLVIDO);
            assertThat(locacao.podeDevolver()).isFalse();
        }

        @Test
        @DisplayName("podeCancelar() = true apenas quando RESERVADO")
        void podeCancelar() {
            locacao.setStatus(StatusLocacao.RESERVADO);
            assertThat(locacao.podeCancelar()).isTrue();

            locacao.setStatus(StatusLocacao.RETIRADO);
            assertThat(locacao.podeCancelar()).isFalse();

            locacao.setStatus(StatusLocacao.DEVOLVIDO);
            assertThat(locacao.podeCancelar()).isFalse();

            locacao.setStatus(StatusLocacao.CANCELADO);
            assertThat(locacao.podeCancelar()).isFalse();
        }

        @Test
        @DisplayName("estaAtrasada() = true quando devolucaoReal eh apos dataFim")
        void estaAtrasada() {
            locacao.setDataFim(LocalDate.now().minusDays(1));
            assertThat(locacao.estaAtrasada(LocalDateTime.now())).isTrue();
        }

        @Test
        @DisplayName("estaAtrasada() = false quando devolucaoReal eh no prazo")
        void naoEstaAtrasada() {
            locacao.setDataFim(LocalDate.now().plusDays(1));
            assertThat(locacao.estaAtrasada(LocalDateTime.now())).isFalse();
        }
    }
}
