package com.jtarcio.closetrental.modules.locacoes;

import com.jtarcio.closetrental.modules.catalogo.domain.model.Produto;
import com.jtarcio.closetrental.modules.catalogo.domain.ports.ProdutoRepositoryPort;
import com.jtarcio.closetrental.modules.estoque.application.EstoqueService;
import com.jtarcio.closetrental.modules.estoque.domain.model.MovimentacaoEstoque;
import com.jtarcio.closetrental.modules.estoque.domain.model.TipoMovimentacao;
import com.jtarcio.closetrental.modules.locacoes.application.RegistrarDevolucaoService;
import com.jtarcio.closetrental.modules.locacoes.domain.model.DanoProduto;
import com.jtarcio.closetrental.modules.locacoes.domain.model.ItemLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusCaucao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.DanoProdutoRepositoryPort;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.modules.notificacoes.application.NotificacaoContextBuilder;
import com.jtarcio.closetrental.modules.notificacoes.application.NotificacaoService;
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
import java.util.List;
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
    @Mock private DanoProdutoRepositoryPort danoPort;
    @Mock private NotificacaoService notificacaoService;
    @Mock private NotificacaoContextBuilder contextBuilder;

    @InjectMocks
    private RegistrarDevolucaoService service;

    private Locacao locacaoRetirada;
    private Produto produto;

    private ItemLocacao criarItem(Long id, Long produtoId, BigDecimal valorDiaria) {
        ItemLocacao item = new ItemLocacao();
        item.setId(id);
        item.setProdutoId(produtoId);
        item.setValorDiaria(valorDiaria);
        return item;
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "percentualMultaDiaria", new BigDecimal("0.10"));

        produto = new Produto();
        produto.setId(10L);
        produto.setValorDiaria(new BigDecimal("100.00"));
        produto.setDisponivel(false);

        locacaoRetirada = new Locacao();
        locacaoRetirada.setId(1L);
        locacaoRetirada.setItens(List.of(criarItem(100L, 10L, new BigDecimal("100.00"))));
        locacaoRetirada.setStatus(StatusLocacao.RETIRADO);
        locacaoRetirada.setCaucao(new BigDecimal("100.00"));
        locacaoRetirada.setMulta(BigDecimal.ZERO);
        locacaoRetirada.setDataInicio(LocalDate.now().minusDays(5));
        locacaoRetirada.setDataFim(LocalDate.now().minusDays(1));

        lenient().when(contextBuilder.construir(any())).thenReturn(java.util.Optional.empty());

        MovimentacaoEstoque movStub = new MovimentacaoEstoque(10L, 1L, TipoMovimentacao.ENTRADA, "stub", "sistema");
        lenient().when(estoqueService.registrarEntrada(anyLong(), anyLong(), anyString())).thenReturn(movStub);
    }

    @Nested
    @DisplayName("Devolucao no prazo")
    class SemAtraso {

        @Test
        @DisplayName("deve registrar devolucao sem multa quando devolvido no prazo")
        void deveRegistrarDevolucaoSemMulta() {
            locacaoRetirada.setDataFim(LocalDate.now());

            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));
            when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produto));
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
            when(produtoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Locacao resultado = service.executar(1L, null, null, null);

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
                assertThat(p.isDisponivel()).isTrue();
                return p;
            });

            service.executar(1L, null, null, null);

            verify(produtoPort).salvar(argThat(p -> p.isDisponivel()));
        }
    }

    @Nested
    @DisplayName("Devolucao com atraso")
    class ComAtraso {

        @Test
        @DisplayName("deve calcular multa com 1 dia de atraso: 10% * 100 * 1 = 10")
        void deveCalcularMultaUmDiaAtraso() {
            locacaoRetirada.setDataFim(LocalDate.now().minusDays(1));

            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));
            when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produto));
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
            when(produtoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Locacao resultado = service.executar(1L, null, null, null);

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

            Locacao resultado = service.executar(1L, null, null, null);

            assertThat(resultado.getMulta()).isEqualByComparingTo("30.00");
        }

        @Test
        @DisplayName("deve calcular multa com 2 produtos e 1 dia de atraso: 10% * (100+80) * 1 = 18")
        void deveCalcularMultaMultiProduto() {
            locacaoRetirada.setItens(List.of(
                    criarItem(100L, 10L, new BigDecimal("100.00")),
                    criarItem(200L, 20L, new BigDecimal("80.00"))
            ));
            locacaoRetirada.setDataFim(LocalDate.now().minusDays(1));

            Produto produto2 = new Produto();
            produto2.setId(20L);
            produto2.setDisponivel(false);
            produto2.setValorDiaria(new BigDecimal("80.00"));

            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));
            when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produto));
            when(produtoPort.buscarPorId(20L)).thenReturn(Optional.of(produto2));
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
            when(produtoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Locacao resultado = service.executar(1L, null, null, null);

            assertThat(resultado.getMulta()).isEqualByComparingTo("18.00");
        }

        @Test
        @DisplayName("deve salvar observacoes quando informadas na devolucao")
        void deveSalvarObservacoes() {
            locacaoRetirada.setDataFim(LocalDate.now());
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));
            when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produto));
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
            when(produtoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Locacao resultado = service.executar(1L, "Peca com pequena mancha", null, null);

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

            assertThatThrownBy(() -> service.executar(1L, null, null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("RETIRADO");
        }

        @Test
        @DisplayName("deve lancar BusinessException ao devolver locacao ja DEVOLVIDA")
        void deveLancarExcecaoSeJaDevolvida() {
            locacaoRetirada.setStatus(StatusLocacao.DEVOLVIDO);
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));

            assertThatThrownBy(() -> service.executar(1L, null, null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("RETIRADO");
        }

        @Test
        @DisplayName("deve lancar BusinessException ao devolver locacao CANCELADA")
        void deveLancarExcecaoSeCancelada() {
            locacaoRetirada.setStatus(StatusLocacao.CANCELADO);
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));

            assertThatThrownBy(() -> service.executar(1L, null, null, null))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("deve lancar EntityNotFoundException quando locacao nao existe")
        void deveLancarEntityNotFound() {
            when(locacaoPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.executar(99L, null, null, null))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("Gestao de caucao")
    class GestaCaucao {

        private void setupDevolucaoNoPrazo() {
            locacaoRetirada.setDataFim(LocalDate.now());
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));
            when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produto));
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
            when(produtoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        @DisplayName("deve marcar caucao como LIBERADA quando sem retencao")
        void deveLiberar() {
            setupDevolucaoNoPrazo();
            Locacao resultado = service.executar(1L, null, null, null);
            assertThat(resultado.getStatusCaucao()).isEqualTo(StatusCaucao.LIBERADA);
            assertThat(resultado.getCaucaoRetida()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("deve marcar caucao como RETIDA_PARCIAL quando retencao < caucao")
        void deveReterParcialmente() {
            setupDevolucaoNoPrazo();
            Locacao resultado = service.executar(1L, null, null, new BigDecimal("40.00"));
            assertThat(resultado.getStatusCaucao()).isEqualTo(StatusCaucao.RETIDA_PARCIAL);
            assertThat(resultado.getCaucaoRetida()).isEqualByComparingTo("40.00");
        }

        @Test
        @DisplayName("deve marcar caucao como RETIDA_TOTAL quando retencao >= caucao")
        void deveReterTotal() {
            setupDevolucaoNoPrazo();
            Locacao resultado = service.executar(1L, null, null, new BigDecimal("100.00"));
            assertThat(resultado.getStatusCaucao()).isEqualTo(StatusCaucao.RETIDA_TOTAL);
            assertThat(resultado.getCaucaoRetida()).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("deve marcar caucao como RETIDA_TOTAL quando retencao excede caucao (teto = caucao)")
        void deveReterTotalQuandoExcede() {
            setupDevolucaoNoPrazo();
            Locacao resultado = service.executar(1L, null, null, new BigDecimal("200.00"));
            assertThat(resultado.getStatusCaucao()).isEqualTo(StatusCaucao.RETIDA_TOTAL);
            assertThat(resultado.getCaucaoRetida()).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("deve marcar caucao como LIBERADA quando retencao e zero explicitamente")
        void deveLiberarComZeroExplicito() {
            setupDevolucaoNoPrazo();
            Locacao resultado = service.executar(1L, null, null, BigDecimal.ZERO);
            assertThat(resultado.getStatusCaucao()).isEqualTo(StatusCaucao.LIBERADA);
            assertThat(resultado.getCaucaoRetida()).isEqualByComparingTo("0.00");
        }
    }

    @Nested
    @DisplayName("Registro de danos")
    class RegistroDanos {

        private void setupDevolucaoNoPrazo() {
            locacaoRetirada.setDataFim(LocalDate.now());
            lenient().when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));
            lenient().when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produto));
            lenient().when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
            lenient().when(produtoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
            lenient().when(danoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        }

        private DanoProduto criarDano(Long itemId, BigDecimal valor) {
            DanoProduto dano = new DanoProduto();
            dano.setItemLocacaoId(itemId);
            dano.setDescricao("Mancha no tecido");
            dano.setValorDesconto(valor);
            return dano;
        }

        @Test
        @DisplayName("deve persistir dano e reter caucao pelo valor do dano automaticamente")
        void deveReterCaucaoPeloDano() {
            setupDevolucaoNoPrazo();
            List<DanoProduto> danos = List.of(criarDano(100L, new BigDecimal("50.00")));

            Locacao resultado = service.executar(1L, null, danos, null);

            verify(danoPort).salvar(any(DanoProduto.class));
            assertThat(resultado.getStatusCaucao()).isEqualTo(StatusCaucao.RETIDA_PARCIAL);
            assertThat(resultado.getCaucaoRetida()).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("deve reter total quando soma dos danos >= caucao")
        void deveReterTotalQuandoDanosSomamMaisQueCaucao() {
            setupDevolucaoNoPrazo();
            List<DanoProduto> danos = List.of(criarDano(100L, new BigDecimal("120.00")));

            Locacao resultado = service.executar(1L, null, danos, null);

            assertThat(resultado.getStatusCaucao()).isEqualTo(StatusCaucao.RETIDA_TOTAL);
            assertThat(resultado.getCaucaoRetida()).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("deve usar override valorRetencaoCaucao mesmo com danos informados")
        void deveUsarOverrideQuandoInformado() {
            setupDevolucaoNoPrazo();
            List<DanoProduto> danos = List.of(criarDano(100L, new BigDecimal("50.00")));

            // override manual: R$30 (menos que o dano de R$50)
            Locacao resultado = service.executar(1L, null, danos, new BigDecimal("30.00"));

            verify(danoPort).salvar(any(DanoProduto.class));
            assertThat(resultado.getStatusCaucao()).isEqualTo(StatusCaucao.RETIDA_PARCIAL);
            assertThat(resultado.getCaucaoRetida()).isEqualByComparingTo("30.00");
        }

        @Test
        @DisplayName("deve lancar BusinessException quando item do dano nao pertence a locacao")
        void deveLancarExcecaoItemInvalido() {
            setupDevolucaoNoPrazo();
            // itemId 999 não existe na locação
            List<DanoProduto> danos = List.of(criarDano(999L, new BigDecimal("50.00")));

            assertThatThrownBy(() -> service.executar(1L, null, danos, null))
                    .isInstanceOf(com.jtarcio.closetrental.shared.exception.BusinessException.class)
                    .hasMessageContaining("999");
        }
    }
}
