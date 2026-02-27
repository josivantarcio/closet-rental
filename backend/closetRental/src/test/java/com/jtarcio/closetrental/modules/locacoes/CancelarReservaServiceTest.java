package com.jtarcio.closetrental.modules.locacoes;

import com.jtarcio.closetrental.modules.locacoes.application.CancelarReservaService;
import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelarReservaService")
class CancelarReservaServiceTest {

    @Mock private LocacaoRepositoryPort locacaoPort;

    @InjectMocks
    private CancelarReservaService service;

    private Locacao locacaoReservada;

    @BeforeEach
    void setUp() {
        locacaoReservada = new Locacao();
        locacaoReservada.setId(1L);
        locacaoReservada.setStatus(StatusLocacao.RESERVADO);
    }

    @Test
    @DisplayName("deve cancelar reserva no status RESERVADO")
    void deveCancelarReservada() {
        when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoReservada));
        when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Locacao resultado = service.executar(1L, "Cliente desistiu");

        assertThat(resultado.getStatus()).isEqualTo(StatusLocacao.CANCELADO);
        assertThat(resultado.getObservacoes()).isEqualTo("Cliente desistiu");
    }

    @Test
    @DisplayName("deve cancelar sem motivo quando motivo e null")
    void deveCancelarSemMotivo() {
        when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoReservada));
        when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Locacao resultado = service.executar(1L, null);

        assertThat(resultado.getStatus()).isEqualTo(StatusLocacao.CANCELADO);
    }

    @Test
    @DisplayName("deve lancar BusinessException ao cancelar locacao RETIRADA")
    void deveLancarExcecaoSeRetirada() {
        locacaoReservada.setStatus(StatusLocacao.RETIRADO);
        when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoReservada));

        assertThatThrownBy(() -> service.executar(1L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("RESERVADO");
    }

    @Test
    @DisplayName("deve lancar BusinessException ao cancelar locacao DEVOLVIDA")
    void deveLancarExcecaoSeDevolvida() {
        locacaoReservada.setStatus(StatusLocacao.DEVOLVIDO);
        when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoReservada));

        assertThatThrownBy(() -> service.executar(1L, null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("deve lancar BusinessException ao cancelar locacao ja CANCELADA")
    void deveLancarExcecaoSeCancelada() {
        locacaoReservada.setStatus(StatusLocacao.CANCELADO);
        when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoReservada));

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
