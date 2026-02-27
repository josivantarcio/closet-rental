package com.jtarcio.closetrental.modules.pagamentos;

import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.modules.pagamentos.application.ProcessarPagamentoService;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.MetodoPagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.Pagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.StatusPagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.ports.GatewayPagamentoPort;
import com.jtarcio.closetrental.modules.pagamentos.domain.ports.GatewayPagamentoPort.PagamentoResultado;
import com.jtarcio.closetrental.modules.pagamentos.domain.ports.PagamentoRepositoryPort;
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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessarPagamentoService")
class ProcessarPagamentoServiceTest {

    @Mock private LocacaoRepositoryPort locacaoPort;
    @Mock private PagamentoRepositoryPort pagamentoPort;
    @Mock private GatewayPagamentoPort gatewayPort;

    @InjectMocks
    private ProcessarPagamentoService service;

    private Locacao locacaoRetirada;
    private Locacao locacaoDevolvida;

    /** Stub de salvar compartilhado: atribui ID na primeira chamada, retorna o objeto nas demais. */
    private void configurarSalvarPagamento() {
        when(pagamentoPort.salvar(any())).thenAnswer(inv -> {
            Pagamento p = inv.getArgument(0);
            if (p.getId() == null) p.setId(10L);
            return p;
        });
    }

    @BeforeEach
    void setUp() {
        locacaoRetirada = new Locacao();
        locacaoRetirada.setId(1L);
        locacaoRetirada.setStatus(StatusLocacao.RETIRADO);

        locacaoDevolvida = new Locacao();
        locacaoDevolvida.setId(2L);
        locacaoDevolvida.setStatus(StatusLocacao.DEVOLVIDO);
    }

    // ─────────────────────────── Aprovação / Falha ───────────────────────────

    @Nested
    @DisplayName("resultado do gateway")
    class ResultadoGateway {

        @Test
        @DisplayName("deve retornar pagamento APROVADO quando gateway retorna sucesso")
        void deveAprovar() {
            configurarSalvarPagamento();
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));
            LocalDateTime agora = LocalDateTime.now();
            when(gatewayPort.processar(any())).thenReturn(new PagamentoResultado(true, "Aprovado", agora));

            Pagamento resultado = service.executar(1L, new BigDecimal("150.00"), MetodoPagamento.PIX, "atendente");

            assertThat(resultado.getStatus()).isEqualTo(StatusPagamento.APROVADO);
            assertThat(resultado.getProcessadoEm()).isEqualTo(agora);
            assertThat(resultado.getMotivo()).isEqualTo("Aprovado");
        }

        @Test
        @DisplayName("deve retornar pagamento com FALHA quando gateway retorna insucesso")
        void deveFalhar() {
            configurarSalvarPagamento();
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));
            LocalDateTime agora = LocalDateTime.now();
            when(gatewayPort.processar(any())).thenReturn(new PagamentoResultado(false, "Cartao recusado", agora));

            Pagamento resultado = service.executar(1L, new BigDecimal("150.00"), MetodoPagamento.CARTAO_CREDITO, "atendente");

            assertThat(resultado.getStatus()).isEqualTo(StatusPagamento.FALHA);
            assertThat(resultado.getMotivo()).isEqualTo("Cartao recusado");
        }

        @Test
        @DisplayName("deve preencher processadoEm apos processamento do gateway")
        void devePreencherProcessadoEm() {
            configurarSalvarPagamento();
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));
            LocalDateTime ts = LocalDateTime.of(2026, 2, 27, 10, 0);
            when(gatewayPort.processar(any())).thenReturn(new PagamentoResultado(true, "OK", ts));

            Pagamento resultado = service.executar(1L, new BigDecimal("200.00"), MetodoPagamento.DINHEIRO, "admin");

            assertThat(resultado.getProcessadoEm()).isEqualTo(ts);
            assertThat(resultado.foiProcessado()).isTrue();
        }
    }

    // ─────────────────────────── Validação de Locação ───────────────────────────

    @Nested
    @DisplayName("validacao de locacao")
    class ValidacaoLocacao {

        @Test
        @DisplayName("deve lancar EntityNotFoundException quando locacao nao existe")
        void deveLancarEntityNotFoundException() {
            when(locacaoPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.executar(99L, new BigDecimal("100.00"), MetodoPagamento.PIX, "admin"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("deve lancar BusinessException para locacao com status RESERVADO")
        void deveLancarExcecaoParaReservado() {
            Locacao reservada = new Locacao();
            reservada.setId(3L);
            reservada.setStatus(StatusLocacao.RESERVADO);
            when(locacaoPort.buscarPorId(3L)).thenReturn(Optional.of(reservada));

            assertThatThrownBy(() -> service.executar(3L, new BigDecimal("100.00"), MetodoPagamento.PIX, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("RESERVADO");
        }

        @Test
        @DisplayName("deve lancar BusinessException para locacao com status CANCELADO")
        void deveLancarExcecaoParaCancelado() {
            Locacao cancelada = new Locacao();
            cancelada.setId(4L);
            cancelada.setStatus(StatusLocacao.CANCELADO);
            when(locacaoPort.buscarPorId(4L)).thenReturn(Optional.of(cancelada));

            assertThatThrownBy(() -> service.executar(4L, new BigDecimal("100.00"), MetodoPagamento.PIX, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("CANCELADO");
        }

        @Test
        @DisplayName("deve processar pagamento para locacao com status RETIRADO")
        void deveProcessarParaRetirado() {
            configurarSalvarPagamento();
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));
            when(gatewayPort.processar(any())).thenReturn(
                    new PagamentoResultado(true, "OK", LocalDateTime.now()));

            Pagamento resultado = service.executar(1L, new BigDecimal("100.00"), MetodoPagamento.PIX, "admin");

            assertThat(resultado.getLocacaoId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("deve processar pagamento para locacao com status DEVOLVIDO")
        void deveProcessarParaDevolvido() {
            configurarSalvarPagamento();
            when(locacaoPort.buscarPorId(2L)).thenReturn(Optional.of(locacaoDevolvida));
            when(gatewayPort.processar(any())).thenReturn(
                    new PagamentoResultado(true, "OK", LocalDateTime.now()));

            Pagamento resultado = service.executar(2L, new BigDecimal("100.00"), MetodoPagamento.PIX, "admin");

            assertThat(resultado.getLocacaoId()).isEqualTo(2L);
        }
    }

    // ─────────────────────────── Validação de Valor ───────────────────────────

    @Nested
    @DisplayName("validacao de valor")
    class ValidacaoValor {

        @Test
        @DisplayName("deve lancar BusinessException para valor zero")
        void deveLancarExcecaoParaValorZero() {
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));

            assertThatThrownBy(() -> service.executar(1L, BigDecimal.ZERO, MetodoPagamento.PIX, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("positivo");
        }

        @Test
        @DisplayName("deve lancar BusinessException para valor negativo")
        void deveLancarExcecaoParaValorNegativo() {
            when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoRetirada));

            assertThatThrownBy(() -> service.executar(1L, new BigDecimal("-50.00"), MetodoPagamento.PIX, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("positivo");
        }
    }
}
