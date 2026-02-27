package com.jtarcio.closetrental.modules.locacoes;

import com.jtarcio.closetrental.modules.catalogo.domain.model.Produto;
import com.jtarcio.closetrental.modules.catalogo.domain.ports.ProdutoRepositoryPort;
import com.jtarcio.closetrental.modules.clientes.domain.model.Cliente;
import com.jtarcio.closetrental.modules.clientes.domain.ports.ClienteRepositoryPort;
import com.jtarcio.closetrental.modules.locacoes.application.CriarReservaService;
import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CriarReservaService")
class CriarReservaServiceTest {

    @Mock private LocacaoRepositoryPort locacaoPort;
    @Mock private ProdutoRepositoryPort produtoPort;
    @Mock private ClienteRepositoryPort clientePort;

    @InjectMocks
    private CriarReservaService service;

    private Cliente clienteAtivo;
    private Produto produtoDisponivel;

    private final LocalDate AMANHA = LocalDate.now().plusDays(1);
    private final LocalDate SEMANA = LocalDate.now().plusDays(7);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "caucaoPadrao", new BigDecimal("100.00"));

        clienteAtivo = new Cliente();
        clienteAtivo.setId(1L);
        clienteAtivo.setNome("Maria");
        clienteAtivo.setAtivo(true);

        produtoDisponivel = new Produto();
        produtoDisponivel.setId(10L);
        produtoDisponivel.setNome("Vestido Longo");
        produtoDisponivel.setValorDiaria(new BigDecimal("89.00"));
        produtoDisponivel.setDisponivel(true);
    }

    @Nested
    @DisplayName("Cenarios de sucesso")
    class Sucesso {

        @Test
        @DisplayName("deve criar reserva com dados validos e calcular valor total corretamente")
        void deveCriarReservaECalcularValorTotal() {
            // 3 dias (amanha + 2): valorTotal = 89 * 3 + 100 = 367
            LocalDate inicio = AMANHA;
            LocalDate fim = AMANHA.plusDays(2);

            when(clientePort.buscarPorId(1L)).thenReturn(Optional.of(clienteAtivo));
            when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produtoDisponivel));
            when(locacaoPort.existeOverlapNoPeriodo(10L, inicio, fim, null)).thenReturn(false);
            when(locacaoPort.salvar(any())).thenAnswer(inv -> {
                Locacao l = inv.getArgument(0);
                l.setId(1L);
                return l;
            });

            Locacao resultado = service.executar(1L, 10L, inicio, fim, null, null);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getStatus()).isEqualTo(StatusLocacao.RESERVADO);
            assertThat(resultado.getValorDiaria()).isEqualByComparingTo("89.00");
            assertThat(resultado.getCaucao()).isEqualByComparingTo("100.00");
            // 89 * 3 + 100 = 367
            assertThat(resultado.getValorTotal()).isEqualByComparingTo("367.00");
            assertThat(resultado.getMulta()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("deve usar caucao customizada quando informada")
        void deveUsarCaucaoCustomizada() {
            when(clientePort.buscarPorId(1L)).thenReturn(Optional.of(clienteAtivo));
            when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produtoDisponivel));
            when(locacaoPort.existeOverlapNoPeriodo(any(), any(), any(), any())).thenReturn(false);
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Locacao resultado = service.executar(1L, 10L, AMANHA, SEMANA, new BigDecimal("200.00"), null);

            assertThat(resultado.getCaucao()).isEqualByComparingTo("200.00");
        }

        @Test
        @DisplayName("deve calcular dias correto (inclusivo): 1 dia = dataInicio == dataFim - 1")
        void deveCalcularDiasInclusivo() {
            // de amanha a depois de amanha = 2 dias
            LocalDate inicio = AMANHA;
            LocalDate fim = AMANHA.plusDays(1);

            when(clientePort.buscarPorId(1L)).thenReturn(Optional.of(clienteAtivo));
            when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produtoDisponivel));
            when(locacaoPort.existeOverlapNoPeriodo(any(), any(), any(), any())).thenReturn(false);
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Locacao resultado = service.executar(1L, 10L, inicio, fim, null, null);

            // 2 dias: 89 * 2 + 100 = 278
            assertThat(resultado.getValorTotal()).isEqualByComparingTo("278.00");
        }
    }

    @Nested
    @DisplayName("Validacao de datas")
    class ValidacaoDatas {

        @Test
        @DisplayName("deve lancar BusinessException quando dataInicio == dataFim")
        void deveLancarExcecaoQuandoDatasIguais() {
            assertThatThrownBy(() -> service.executar(1L, 10L, AMANHA, AMANHA, null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("anterior");
        }

        @Test
        @DisplayName("deve lancar BusinessException quando dataInicio > dataFim")
        void deveLancarExcecaoQuandoInicioDepoisFim() {
            assertThatThrownBy(() -> service.executar(1L, 10L, SEMANA, AMANHA, null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("anterior");
        }

        @Test
        @DisplayName("deve lancar BusinessException quando dataInicio e no passado")
        void deveLancarExcecaoQuandoInicioNoPassado() {
            LocalDate ontem = LocalDate.now().minusDays(1);
            assertThatThrownBy(() -> service.executar(1L, 10L, ontem, SEMANA, null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("passado");
        }
    }

    @Nested
    @DisplayName("Validacao de cliente")
    class ValidacaoCliente {

        @Test
        @DisplayName("deve lancar EntityNotFoundException quando cliente nao existe")
        void deveLancarExcecaoClienteNaoEncontrado() {
            when(clientePort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.executar(99L, 10L, AMANHA, SEMANA, null, null))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("deve lancar BusinessException quando cliente esta inativo")
        void deveLancarExcecaoClienteInativo() {
            clienteAtivo.setAtivo(false);
            when(clientePort.buscarPorId(1L)).thenReturn(Optional.of(clienteAtivo));

            assertThatThrownBy(() -> service.executar(1L, 10L, AMANHA, SEMANA, null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("inativo");
        }
    }

    @Nested
    @DisplayName("Validacao de produto")
    class ValidacaoProduto {

        @Test
        @DisplayName("deve lancar EntityNotFoundException quando produto nao existe")
        void deveLancarExcecaoProdutoNaoEncontrado() {
            when(clientePort.buscarPorId(1L)).thenReturn(Optional.of(clienteAtivo));
            when(produtoPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.executar(1L, 99L, AMANHA, SEMANA, null, null))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("deve lancar BusinessException quando produto esta indisponivel")
        void deveLancarExcecaoProdutoIndisponivel() {
            produtoDisponivel.setDisponivel(false);
            when(clientePort.buscarPorId(1L)).thenReturn(Optional.of(clienteAtivo));
            when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produtoDisponivel));

            assertThatThrownBy(() -> service.executar(1L, 10L, AMANHA, SEMANA, null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("disponivel");
        }
    }

    @Nested
    @DisplayName("Validacao de disponibilidade - regra de overlap")
    class ValidacaoOverlap {

        @Test
        @DisplayName("deve lancar ConflictException quando existe overlap de periodo")
        void deveLancarExcecaoOverlap() {
            when(clientePort.buscarPorId(1L)).thenReturn(Optional.of(clienteAtivo));
            when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produtoDisponivel));
            when(locacaoPort.existeOverlapNoPeriodo(10L, AMANHA, SEMANA, null)).thenReturn(true);

            assertThatThrownBy(() -> service.executar(1L, 10L, AMANHA, SEMANA, null, null))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining(AMANHA.toString())
                    .hasMessageContaining(SEMANA.toString());
        }

        @Test
        @DisplayName("deve criar reserva quando nao ha overlap")
        void deveCriarQuandoSemOverlap() {
            when(clientePort.buscarPorId(1L)).thenReturn(Optional.of(clienteAtivo));
            when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produtoDisponivel));
            when(locacaoPort.existeOverlapNoPeriodo(10L, AMANHA, SEMANA, null)).thenReturn(false);
            when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThatCode(() -> service.executar(1L, 10L, AMANHA, SEMANA, null, null))
                    .doesNotThrowAnyException();
        }
    }
}
