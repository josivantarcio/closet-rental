package com.jtarcio.closetrental.modules.locacoes;

import com.jtarcio.closetrental.modules.locacoes.domain.model.ItemLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Locacao domain model")
class LocacaoDomainTest {

    private Locacao locacao;

    private ItemLocacao criarItem(BigDecimal valorDiaria) {
        ItemLocacao item = new ItemLocacao();
        item.setProdutoId(1L);
        item.setValorDiaria(valorDiaria);
        return item;
    }

    @BeforeEach
    void setUp() {
        locacao = new Locacao();
        locacao.setCaucao(new BigDecimal("150.00"));
        locacao.setMulta(BigDecimal.ZERO);
        // Item padrão: 1 produto com diária de R$100
        locacao.setItens(List.of(criarItem(new BigDecimal("100.00"))));
    }

    @Nested
    @DisplayName("calcularDias()")
    class CalcularDias {

        @Test
        @DisplayName("deve calcular 2 dias quando inicio e fim diferem por 1 dia (inclusivo)")
        void umDia() {
            locacao.setDataInicio(LocalDate.of(2026, 3, 10));
            locacao.setDataFim(LocalDate.of(2026, 3, 11));
            assertThat(locacao.calcularDias()).isEqualTo(2);
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
        @DisplayName("deve calcular valorTotal = soma(diarias * dias) + caucao — 1 item")
        void calcularValorTotalUmItem() {
            locacao.setDataInicio(LocalDate.of(2026, 3, 10));
            locacao.setDataFim(LocalDate.of(2026, 3, 12)); // 3 dias
            // 100 * 3 + 150 = 450
            assertThat(locacao.calcularValorTotal()).isEqualByComparingTo("450.00");
        }

        @Test
        @DisplayName("deve calcular valorTotal com 2 itens — soma das diárias")
        void calcularValorTotalDoisItens() {
            locacao.setItens(List.of(
                    criarItem(new BigDecimal("100.00")),
                    criarItem(new BigDecimal("80.00"))
            ));
            locacao.setDataInicio(LocalDate.of(2026, 3, 10));
            locacao.setDataFim(LocalDate.of(2026, 3, 12)); // 3 dias
            // (100 + 80) * 3 + 150 = 540 + 150 = 690
            assertThat(locacao.calcularValorTotal()).isEqualByComparingTo("690.00");
        }

        @Test
        @DisplayName("deve calcular valorTotal sem caucao (null tratado como zero)")
        void calcularSemCaucao() {
            locacao.setCaucao(null);
            locacao.setDataInicio(LocalDate.of(2026, 3, 10));
            locacao.setDataFim(LocalDate.of(2026, 3, 12)); // 3 dias
            // 100 * 3 = 300
            assertThat(locacao.calcularValorTotal()).isEqualByComparingTo("300.00");
        }
    }

    @Nested
    @DisplayName("calcularMulta()")
    class CalcularMulta {

        @Test
        @DisplayName("deve retornar ZERO quando nao ha atraso (devolvido antes do prazo)")
        void semAtraso() {
            locacao.setDataFim(LocalDate.now().plusDays(1));
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
        @DisplayName("deve calcular multa com 1 dia de atraso: 10% * 100 * 1 = 10")
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
        @DisplayName("deve calcular multa com 2 itens e 2 dias de atraso: 10% * (100+80) * 2 = 36")
        void multaComDoisItens() {
            locacao.setItens(List.of(
                    criarItem(new BigDecimal("100.00")),
                    criarItem(new BigDecimal("80.00"))
            ));
            locacao.setDataFim(LocalDate.now().minusDays(2));
            BigDecimal multa = locacao.calcularMulta(LocalDateTime.now(), new BigDecimal("0.10"));
            assertThat(multa).isEqualByComparingTo("36.00");
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

    @Nested
    @DisplayName("getProdutoIds()")
    class GetProdutoIds {

        @Test
        @DisplayName("deve retornar lista de ids de todos os itens")
        void retornaTodosIds() {
            ItemLocacao item1 = criarItem(BigDecimal.TEN);
            item1.setProdutoId(10L);
            ItemLocacao item2 = criarItem(BigDecimal.TEN);
            item2.setProdutoId(20L);
            locacao.setItens(List.of(item1, item2));

            assertThat(locacao.getProdutoIds()).containsExactly(10L, 20L);
        }
    }
}
