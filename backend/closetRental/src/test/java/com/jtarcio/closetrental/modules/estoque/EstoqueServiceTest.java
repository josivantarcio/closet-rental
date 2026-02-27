package com.jtarcio.closetrental.modules.estoque;

import com.jtarcio.closetrental.modules.catalogo.domain.model.Produto;
import com.jtarcio.closetrental.modules.catalogo.domain.ports.ProdutoRepositoryPort;
import com.jtarcio.closetrental.modules.estoque.application.EstoqueService;
import com.jtarcio.closetrental.modules.estoque.domain.model.MovimentacaoEstoque;
import com.jtarcio.closetrental.modules.estoque.domain.model.TipoMovimentacao;
import com.jtarcio.closetrental.modules.estoque.domain.ports.EstoqueRepositoryPort;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EstoqueService")
class EstoqueServiceTest {

    @Mock private EstoqueRepositoryPort estoquePort;
    @Mock private ProdutoRepositoryPort produtoPort;

    @InjectMocks
    private EstoqueService service;

    private Produto produtoDisponivel;
    private Produto produtoIndisponivel;

    @BeforeEach
    void setUp() {
        produtoDisponivel = new Produto();
        produtoDisponivel.setId(1L);
        produtoDisponivel.setNome("Vestido Longo");
        produtoDisponivel.setCategoria("Vestido");
        produtoDisponivel.setValorDiaria(new BigDecimal("89.00"));
        produtoDisponivel.setDisponivel(true);

        produtoIndisponivel = new Produto();
        produtoIndisponivel.setId(2L);
        produtoIndisponivel.setNome("Terno Social");
        produtoIndisponivel.setCategoria("Terno");
        produtoIndisponivel.setValorDiaria(new BigDecimal("65.00"));
        produtoIndisponivel.setDisponivel(false);
    }

    // ─────────────────────────── SAIDA ───────────────────────────

    @Nested
    @DisplayName("registrarSaida()")
    class RegistrarSaida {

        @Test
        @DisplayName("deve registrar saida com tipo SAIDA e locacaoId correto")
        void deveRegistrarSaida() {
            when(produtoPort.buscarPorId(1L)).thenReturn(Optional.of(produtoDisponivel));
            when(estoquePort.salvar(any())).thenAnswer(inv -> {
                MovimentacaoEstoque m = inv.getArgument(0);
                m.setId(10L);
                return m;
            });

            MovimentacaoEstoque resultado = service.registrarSaida(1L, 99L, "atendente");

            assertThat(resultado.getTipo()).isEqualTo(TipoMovimentacao.SAIDA);
            assertThat(resultado.getProdutoId()).isEqualTo(1L);
            assertThat(resultado.getLocacaoId()).isEqualTo(99L);
            assertThat(resultado.getMotivo()).contains("99");
            verify(estoquePort).salvar(any());
        }

        @Test
        @DisplayName("deve lancar EntityNotFoundException ao registrar saida de produto inexistente")
        void deveLancarExcecaoProdutoInexistente() {
            when(produtoPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.registrarSaida(99L, 1L, "sistema"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ─────────────────────────── ENTRADA ───────────────────────────

    @Nested
    @DisplayName("registrarEntrada()")
    class RegistrarEntrada {

        @Test
        @DisplayName("deve registrar entrada com tipo ENTRADA e locacaoId correto")
        void deveRegistrarEntrada() {
            when(produtoPort.buscarPorId(1L)).thenReturn(Optional.of(produtoIndisponivel));
            when(estoquePort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            MovimentacaoEstoque resultado = service.registrarEntrada(1L, 99L, "sistema");

            assertThat(resultado.getTipo()).isEqualTo(TipoMovimentacao.ENTRADA);
            assertThat(resultado.getLocacaoId()).isEqualTo(99L);
        }
    }

    // ─────────────────────────── MANUTENCAO ───────────────────────────

    @Nested
    @DisplayName("enviarParaManutencao()")
    class EnviarManutencao {

        @Test
        @DisplayName("deve enviar peca disponivel para manutencao e marca-la indisponivel")
        void deveEnviarParaManutencao() {
            when(produtoPort.buscarPorId(1L)).thenReturn(Optional.of(produtoDisponivel));
            when(produtoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
            when(estoquePort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            MovimentacaoEstoque resultado = service.enviarParaManutencao(1L, "Limpeza pos-evento", "admin");

            assertThat(resultado.getTipo()).isEqualTo(TipoMovimentacao.MANUTENCAO);
            assertThat(resultado.getMotivo()).isEqualTo("Limpeza pos-evento");
            // Produto deve ter sido marcado indisponivel
            verify(produtoPort).salvar(argThat(p -> !p.isDisponivel()));
        }

        @Test
        @DisplayName("deve usar motivo padrao quando motivo e null")
        void deveUsarMotivoPadrao() {
            when(produtoPort.buscarPorId(1L)).thenReturn(Optional.of(produtoDisponivel));
            when(produtoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
            when(estoquePort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            MovimentacaoEstoque resultado = service.enviarParaManutencao(1L, null, "admin");

            assertThat(resultado.getMotivo()).isNotBlank();
        }

        @Test
        @DisplayName("deve lancar BusinessException ao enviar peca ja indisponivel para manutencao")
        void deveLancarExcecaoSeJaIndisponivel() {
            when(produtoPort.buscarPorId(2L)).thenReturn(Optional.of(produtoIndisponivel));

            assertThatThrownBy(() -> service.enviarParaManutencao(2L, "Limpeza", "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("indisponivel");

            verify(produtoPort, never()).salvar(any());
            verify(estoquePort, never()).salvar(any());
        }

        @Test
        @DisplayName("deve lancar EntityNotFoundException para produto inexistente")
        void deveLancarExcecaoProdutoNaoEncontrado() {
            when(produtoPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.enviarParaManutencao(99L, "Limpeza", "admin"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ─────────────────────────── RETORNO MANUTENCAO ───────────────────────────

    @Nested
    @DisplayName("retornarDaManutencao()")
    class RetornarManutencao {

        @Test
        @DisplayName("deve retornar peca indisponivel e marca-la disponivel")
        void deveRetornarDaManutencao() {
            when(produtoPort.buscarPorId(2L)).thenReturn(Optional.of(produtoIndisponivel));
            when(produtoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
            when(estoquePort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            MovimentacaoEstoque resultado = service.retornarDaManutencao(2L, "Peca limpa e pronta", "admin");

            assertThat(resultado.getTipo()).isEqualTo(TipoMovimentacao.RETORNO_MANUTENCAO);
            verify(produtoPort).salvar(argThat(p -> p.isDisponivel()));
        }

        @Test
        @DisplayName("deve lancar BusinessException ao retornar peca ja disponivel")
        void deveLancarExcecaoSeJaDisponivel() {
            when(produtoPort.buscarPorId(1L)).thenReturn(Optional.of(produtoDisponivel));

            assertThatThrownBy(() -> service.retornarDaManutencao(1L, "ok", "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("disponivel");
        }
    }

    // ─────────────────────────── DISPONIBILIDADE ───────────────────────────

    @Nested
    @DisplayName("verificarDisponibilidade()")
    class VerificarDisponibilidade {

        @Test
        @DisplayName("deve retornar disponibilidade e total de locacoes")
        void deveRetornarDisponibilidade() {
            when(produtoPort.buscarPorId(1L)).thenReturn(Optional.of(produtoDisponivel));
            when(estoquePort.contarSaidasPorProduto(1L)).thenReturn(7L);

            EstoqueService.DisponibilidadeResponse resp = service.verificarDisponibilidade(1L);

            assertThat(resp.produtoId()).isEqualTo(1L);
            assertThat(resp.nomeProduto()).isEqualTo("Vestido Longo");
            assertThat(resp.disponivel()).isTrue();
            assertThat(resp.totalLocacoes()).isEqualTo(7L);
        }

        @Test
        @DisplayName("deve lancar EntityNotFoundException para produto inexistente")
        void deveLancarExcecaoProdutoNaoEncontrado() {
            when(produtoPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.verificarDisponibilidade(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ─────────────────────────── HISTORICO ───────────────────────────

    @Nested
    @DisplayName("listarHistoricoPorProduto()")
    class HistoricoPorProduto {

        @Test
        @DisplayName("deve retornar pagina de movimentacoes do produto")
        void deveRetornarHistorico() {
            MovimentacaoEstoque mov = new MovimentacaoEstoque(1L, 10L, TipoMovimentacao.SAIDA, "Retirada", "admin");
            when(produtoPort.buscarPorId(1L)).thenReturn(Optional.of(produtoDisponivel));
            when(estoquePort.listarPorProduto(eq(1L), any()))
                    .thenReturn(new PageImpl<>(List.of(mov)));

            var pagina = service.listarHistoricoPorProduto(1L, PageRequest.of(0, 20));

            assertThat(pagina.getContent()).hasSize(1);
            assertThat(pagina.getContent().get(0).getTipo()).isEqualTo(TipoMovimentacao.SAIDA);
        }

        @Test
        @DisplayName("deve lancar EntityNotFoundException para produto inexistente")
        void deveLancarExcecaoProdutoNaoEncontrado() {
            when(produtoPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.listarHistoricoPorProduto(99L, PageRequest.of(0, 20)))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ─────────────────────────── DOMAIN MODEL ───────────────────────────

    @Nested
    @DisplayName("MovimentacaoEstoque domain model")
    class DomainModel {

        @Test
        @DisplayName("eSaida() deve ser true para SAIDA e MANUTENCAO")
        void eSaida() {
            MovimentacaoEstoque saida = new MovimentacaoEstoque();
            saida.setTipo(TipoMovimentacao.SAIDA);
            assertThat(saida.eSaida()).isTrue();

            saida.setTipo(TipoMovimentacao.MANUTENCAO);
            assertThat(saida.eSaida()).isTrue();

            saida.setTipo(TipoMovimentacao.ENTRADA);
            assertThat(saida.eSaida()).isFalse();
        }

        @Test
        @DisplayName("eEntrada() deve ser true para ENTRADA e RETORNO_MANUTENCAO")
        void eEntrada() {
            MovimentacaoEstoque mov = new MovimentacaoEstoque();
            mov.setTipo(TipoMovimentacao.ENTRADA);
            assertThat(mov.eEntrada()).isTrue();

            mov.setTipo(TipoMovimentacao.RETORNO_MANUTENCAO);
            assertThat(mov.eEntrada()).isTrue();

            mov.setTipo(TipoMovimentacao.SAIDA);
            assertThat(mov.eEntrada()).isFalse();
        }
    }
}
