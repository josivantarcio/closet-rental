package com.jtarcio.closetrental.modules.pagamentos;

import com.jtarcio.closetrental.modules.pagamentos.application.EstornarPagamentoService;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.MetodoPagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.Pagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.StatusPagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.ports.PagamentoRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("EstornarPagamentoService")
class EstornarPagamentoServiceTest {

    @Mock private PagamentoRepositoryPort pagamentoPort;

    @InjectMocks
    private EstornarPagamentoService service;

    private Pagamento pagamentoAprovado;

    @BeforeEach
    void setUp() {
        pagamentoAprovado = new Pagamento();
        pagamentoAprovado.setId(1L);
        pagamentoAprovado.setLocacaoId(10L);
        pagamentoAprovado.setValor(new BigDecimal("200.00"));
        pagamentoAprovado.setMetodo(MetodoPagamento.PIX);
        pagamentoAprovado.setStatus(StatusPagamento.APROVADO);
        pagamentoAprovado.setCriadoEm(LocalDateTime.now().minusMinutes(5));
        pagamentoAprovado.setProcessadoEm(LocalDateTime.now().minusMinutes(4));
    }

    @Test
    @DisplayName("deve estornar pagamento APROVADO e alterar status para ESTORNADO")
    void deveEstornarAprovado() {
        when(pagamentoPort.buscarPorId(1L)).thenReturn(Optional.of(pagamentoAprovado));
        when(pagamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Pagamento resultado = service.executar(1L, "Solicitacao do cliente");

        assertThat(resultado.getStatus()).isEqualTo(StatusPagamento.ESTORNADO);
        assertThat(resultado.getMotivo()).isEqualTo("Solicitacao do cliente");
        assertThat(resultado.getProcessadoEm()).isNotNull();
        verify(pagamentoPort).salvar(any());
    }

    @Test
    @DisplayName("deve salvar o motivo do estorno corretamente")
    void deveSalvarMotivo() {
        when(pagamentoPort.buscarPorId(1L)).thenReturn(Optional.of(pagamentoAprovado));
        when(pagamentoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Pagamento resultado = service.executar(1L, "Produto com defeito");

        assertThat(resultado.getMotivo()).isEqualTo("Produto com defeito");
    }

    @Test
    @DisplayName("deve lancar BusinessException ao estornar pagamento PENDENTE")
    void deveLancarExcecaoParaPendente() {
        pagamentoAprovado.setStatus(StatusPagamento.PENDENTE);
        when(pagamentoPort.buscarPorId(1L)).thenReturn(Optional.of(pagamentoAprovado));

        assertThatThrownBy(() -> service.executar(1L, "Motivo"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("APROVADO");

        verify(pagamentoPort, never()).salvar(any());
    }

    @Test
    @DisplayName("deve lancar BusinessException ao estornar pagamento com FALHA")
    void deveLancarExcecaoParaFalha() {
        pagamentoAprovado.setStatus(StatusPagamento.FALHA);
        when(pagamentoPort.buscarPorId(1L)).thenReturn(Optional.of(pagamentoAprovado));

        assertThatThrownBy(() -> service.executar(1L, "Motivo"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("APROVADO");
    }

    @Test
    @DisplayName("deve lancar EntityNotFoundException quando pagamento nao existe")
    void deveLancarEntityNotFound() {
        when(pagamentoPort.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.executar(99L, "Motivo"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }
}
