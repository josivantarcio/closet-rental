package com.jtarcio.closetrental.modules.catalogo;

import com.jtarcio.closetrental.modules.catalogo.api.dto.PeriodoOcupadoResponse;
import com.jtarcio.closetrental.modules.catalogo.application.AgendaProdutoService;
import com.jtarcio.closetrental.modules.catalogo.application.ProdutoService;
import com.jtarcio.closetrental.modules.catalogo.domain.model.Produto;
import com.jtarcio.closetrental.modules.locacoes.domain.model.ItemLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
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
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgendaProdutoService")
class AgendaProdutoServiceTest {

    @Mock
    private ProdutoService produtoService;

    @Mock
    private LocacaoRepositoryPort locacaoRepository;

    @InjectMocks
    private AgendaProdutoService agendaProdutoService;

    private Produto produto;

    @BeforeEach
    void setUp() {
        produto = new Produto();
        produto.setId(1L);
        produto.setNome("Vestido de Festa");
        produto.setValorDiaria(BigDecimal.valueOf(150));
    }

    private Locacao locacaoComPeriodo(Long id, LocalDate inicio, LocalDate fim, StatusLocacao status) {
        ItemLocacao item = new ItemLocacao();
        item.setProdutoId(1L);

        Locacao l = new Locacao();
        l.setId(id);
        l.setItens(java.util.List.of(item));
        l.setDataInicio(inicio);
        l.setDataFim(fim);
        l.setStatus(status);
        return l;
    }

    @Nested
    @DisplayName("buscarAgenda")
    class BuscarAgenda {

        @Test
        @DisplayName("retorna lista vazia quando produto não tem ocupações no mês")
        void semOcupacoes() {
            when(produtoService.buscarPorId(1L)).thenReturn(produto);
            when(locacaoRepository.buscarPeriodosOcupadosNoMes(eq(1L), any(), any()))
                    .thenReturn(List.of());

            List<PeriodoOcupadoResponse> resultado = agendaProdutoService.buscarAgenda(1L, YearMonth.of(2026, 3));

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("retorna períodos ocupados ordenados por dataInicio")
        void comOcupacoes() {
            YearMonth mes = YearMonth.of(2026, 3);

            Locacao l1 = locacaoComPeriodo(10L, LocalDate.of(2026, 3, 5), LocalDate.of(2026, 3, 8), StatusLocacao.RESERVADO);
            Locacao l2 = locacaoComPeriodo(11L, LocalDate.of(2026, 3, 15), LocalDate.of(2026, 3, 20), StatusLocacao.RETIRADO);

            when(produtoService.buscarPorId(1L)).thenReturn(produto);
            when(locacaoRepository.buscarPeriodosOcupadosNoMes(
                    eq(1L),
                    eq(mes.atDay(1)),
                    eq(mes.atEndOfMonth())))
                    .thenReturn(List.of(l1, l2));

            List<PeriodoOcupadoResponse> resultado = agendaProdutoService.buscarAgenda(1L, mes);

            assertThat(resultado).hasSize(2);

            assertThat(resultado.get(0).locacaoId()).isEqualTo(10L);
            assertThat(resultado.get(0).dataInicio()).isEqualTo(LocalDate.of(2026, 3, 5));
            assertThat(resultado.get(0).dataFim()).isEqualTo(LocalDate.of(2026, 3, 8));
            assertThat(resultado.get(0).status()).isEqualTo("RESERVADO");

            assertThat(resultado.get(1).locacaoId()).isEqualTo(11L);
            assertThat(resultado.get(1).status()).isEqualTo("RETIRADO");
        }

        @Test
        @DisplayName("inclui locação que começa antes do mês e termina dentro dele")
        void locacaoQueIniciouAntes() {
            YearMonth mes = YearMonth.of(2026, 3);

            Locacao l = locacaoComPeriodo(20L, LocalDate.of(2026, 2, 25), LocalDate.of(2026, 3, 5), StatusLocacao.RETIRADO);

            when(produtoService.buscarPorId(1L)).thenReturn(produto);
            when(locacaoRepository.buscarPeriodosOcupadosNoMes(any(), any(), any()))
                    .thenReturn(List.of(l));

            List<PeriodoOcupadoResponse> resultado = agendaProdutoService.buscarAgenda(1L, mes);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).dataInicio()).isEqualTo(LocalDate.of(2026, 2, 25));
        }

        @Test
        @DisplayName("inclui locação que começa no mês e termina no mês seguinte")
        void locacaoQueTerminaDepois() {
            YearMonth mes = YearMonth.of(2026, 3);

            Locacao l = locacaoComPeriodo(30L, LocalDate.of(2026, 3, 28), LocalDate.of(2026, 4, 5), StatusLocacao.RESERVADO);

            when(produtoService.buscarPorId(1L)).thenReturn(produto);
            when(locacaoRepository.buscarPeriodosOcupadosNoMes(any(), any(), any()))
                    .thenReturn(List.of(l));

            List<PeriodoOcupadoResponse> resultado = agendaProdutoService.buscarAgenda(1L, mes);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).dataFim()).isEqualTo(LocalDate.of(2026, 4, 5));
        }

        @Test
        @DisplayName("lança EntityNotFoundException quando produto não existe")
        void produtoNaoEncontrado() {
            when(produtoService.buscarPorId(99L))
                    .thenThrow(new EntityNotFoundException("Produto não encontrado com id: 99"));

            assertThatThrownBy(() -> agendaProdutoService.buscarAgenda(99L, YearMonth.of(2026, 3)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");

            verify(locacaoRepository, never()).buscarPeriodosOcupadosNoMes(any(), any(), any());
        }

        @Test
        @DisplayName("passa as datas corretas de início e fim do mês para o repositório")
        void passaDatasCorretas() {
            YearMonth mes = YearMonth.of(2026, 2); // fevereiro — 28 dias em 2026

            when(produtoService.buscarPorId(1L)).thenReturn(produto);
            when(locacaoRepository.buscarPeriodosOcupadosNoMes(any(), any(), any()))
                    .thenReturn(List.of());

            agendaProdutoService.buscarAgenda(1L, mes);

            verify(locacaoRepository).buscarPeriodosOcupadosNoMes(
                    eq(1L),
                    eq(LocalDate.of(2026, 2, 1)),
                    eq(LocalDate.of(2026, 2, 28)));
        }
    }
}
