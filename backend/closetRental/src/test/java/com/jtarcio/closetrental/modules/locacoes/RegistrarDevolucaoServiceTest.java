package com.jtarcio.closetrental.modules.locacoes;

import com.jtarcio.closetrental.modules.catalogo.domain.model.Produto;
import com.jtarcio.closetrental.modules.catalogo.domain.ports.ProdutoRepositoryPort;
import com.jtarcio.closetrental.modules.estoque.application.EstoqueService;
import com.jtarcio.closetrental.modules.estoque.domain.model.MovimentacaoEstoque;
import com.jtarcio.closetrental.modules.estoque.domain.model.TipoMovimentacao;
import com.jtarcio.closetrental.modules.locacoes.application.RegistrarDevolucaoService;
import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegistrarDevolucaoService")
class RegistrarDevolucaoServiceTest {

    @Mock private LocacaoRepositoryPort locacaoPort;
    @Mock private ProdutoRepositoryPort produtoPort;
    @Mock private EstoqueService estoqueService;

    @InjectMocks
    private RegistrarDevolucaoService service;

    private Locacao locacaoRetirada;
    private Produto produto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "percentualMultaDiaria", new BigDecimal("0.10"));

        produto = new Produto();
        produto.setId(10L);
        produto.setValorDiaria(new BigDecimal("100.00"));
        produto.setDisponivel(false);

        locacaoRetirada = new Locacao();
        locacaoRetirada.setId(1L);
        locacaoRetirada.setProdutoId(10L);
        locacaoRetirada.setStatus(StatusLocacao.RETIRADO);
        locacaoRetirada.setValorDiaria(new BigDecimal("100.00"));
        locacaoRetirada.setCaucao(new BigDecimal("100.00"));
        locacaoRetirada.setMulta(BigDecimal.ZERO);
        // Periodo: 5 dias atras ate ontem (devolucao no prazo seria ontem)
        locacaoRetirada.setDataInicio(LocalDate.now().minusDays(5));
        locacaoRetirada.setDataFim(LocalDate.now().minusDays(1));

        // Stub lenient para estoqueService — nao falha se nao chamado em testes de erro
        MovimentacaoEstoque movStub = new MovimentacaoEstoque(10L, 1L, TipoMovimentacao.ENTRADA, "stub", "sistema");
        lenient().when(estoqueService.registrarEntrada(anyLong(), anyLong(), anyString())).thenReturn(movStub);
    }

    @Nested
    @DisplayName("Devolucao no prazo")
    class SemAtraso {

        @Test
        @DisplayName("deve registrar devolucao sem multa quando devolvido no prazo")
        void deveRegistrarDevolucaoSemMulta() {
            // dataFim = ontem, devolvendo hoje = dentro do prazo (nao passou de ontem)
            // Na logica: atraso so quando devolucaoAt.toLocalDate().isAfter(dataFim)
            // hoje NAO isAfter(ontem) => sem multa
            locacaoRetirada.setDataFim(LocalDate.now()); // fim = hoje

            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));
            when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produto));
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
            when(produtoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Locacao resultado = service.executar(1L, null);

            assertThat(resultado.getStatus()).isEqualTo(StatusLocacao.DEVOLVIDO);
            assertThat(resultado.getMulta()).isEqualByComparingTo("0.00");
            assertThat(resultado.getDevolucaoAt()).isNotNull();
        }

        @Test
        @DisplayName("deve marcar produto como disponivel apos devolucao")
        void deveLiberarProdutoAposDevolucao() {
            locacaoRetirada.setDataFim(LocalDate.now());
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));
            when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produto));
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
            when(produtoPort.salvar(any())).thenAnswer(inv -> {
                Produto p = inv.getArgument(0);
                assertThat(p.isDisponivel()).isTrue(); // verificacao inline
                return p;
            });

            service.executar(1L, null);

            verify(produtoPort).salvar(argThat(p -> p.isDisponivel()));
        }
    }

    @Nested
    @DisplayName("Devolucao com atraso")
    class ComAtraso {

        @Test
        @DisplayName("deve calcular multa com 1 dia de atraso: 10% * 100 * 1 = 10")
        void deveCalcularMultaUmDiaAtraso() {
            // dataFim = ontem => devolvendo hoje = 1 dia de atraso
            locacaoRetirada.setDataFim(LocalDate.now().minusDays(1));

            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));
            when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produto));
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
            when(produtoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Locacao resultado = service.executar(1L, null);

            // multa = 0.10 * 100.00 * 1 = 10.00
            assertThat(resultado.getMulta()).isEqualByComparingTo("10.00");
            assertThat(resultado.getStatus()).isEqualTo(StatusLocacao.DEVOLVIDO);
        }

        @Test
        @DisplayName("deve calcular multa com 3 dias de atraso: 10% * 100 * 3 = 30")
        void deveCalcularMultaTresDiasAtraso() {
            locacaoRetirada.setDataFim(LocalDate.now().minusDays(3));

            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));
            when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produto));
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
            when(produtoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Locacao resultado = service.executar(1L, null);

            // multa = 0.10 * 100.00 * 3 = 30.00
            assertThat(resultado.getMulta()).isEqualByComparingTo("30.00");
        }

        @Test
        @DisplayName("deve salvar observacoes quando informadas na devolucao")
        void deveSalvarObservacoes() {
            locacaoRetirada.setDataFim(LocalDate.now());
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));
            when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produto));
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
            when(produtoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Locacao resultado = service.executar(1L, "Peca com pequena mancha");

            assertThat(resultado.getObservacoes()).isEqualTo("Peca com pequena mancha");
        }
    }

    @Nested
    @DisplayName("Validacoes de estado")
    class ValidacoesEstado {

        @Test
        @DisplayName("deve lancar BusinessException ao devolver locacao RESERVADA")
        void deveLancarExcecaoSeReservada() {
            locacaoRetirada.setStatus(StatusLocacao.RESERVADO);
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));

            assertThatThrownBy(() -> service.executar(1L, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("RETIRADO");
        }

        @Test
        @DisplayName("deve lancar BusinessException ao devolver locacao ja DEVOLVIDA")
        void deveLancarExcecaoSeJaDevolvida() {
            locacaoRetirada.setStatus(StatusLocacao.DEVOLVIDO);
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));

            assertThatThrownBy(() -> service.executar(1L, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("RETIRADO");
        }

        @Test
        @DisplayName("deve lancar BusinessException ao devolver locacao CANCELADA")
        void deveLancarExcecaoSeCancelada() {
            locacaoRetirada.setStatus(StatusLocacao.CANCELADO);
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));

            assertThatThrownBy(() -> service.executar(1L, null))
                    .isInstanceOf(BusinessException.class);
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
}
