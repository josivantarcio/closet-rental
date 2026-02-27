package com.jtarcio.closetrental.modules.locacoes;

import com.jtarcio.closetrental.modules.catalogo.domain.model.Produto;
import com.jtarcio.closetrental.modules.catalogo.domain.ports.ProdutoRepositoryPort;
import com.jtarcio.closetrental.modules.locacoes.application.RegistrarRetiradaService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegistrarRetiradaService")
class RegistrarRetiradaServiceTest {

    @Mock private LocacaoRepositoryPort locacaoPort;
    @Mock private ProdutoRepositoryPort produtoPort;

    @InjectMocks
    private RegistrarRetiradaService service;

    private Locacao locacaoReservada;
    private Produto produto;

    @BeforeEach
    void setUp() {
        produto = new Produto();
        produto.setId(10L);
        produto.setDisponivel(true);
        produto.setValorDiaria(new BigDecimal("100.00"));

        locacaoReservada = new Locacao();
        locacaoReservada.setId(1L);
        locacaoReservada.setProdutoId(10L);
        locacaoReservada.setStatus(StatusLocacao.RESERVADO);
        locacaoReservada.setDataInicio(LocalDate.now());
        locacaoReservada.setDataFim(LocalDate.now().plusDays(3));
    }

    @Test
    @DisplayName("deve registrar retirada e marcar produto indisponivel")
    void deveRegistrarRetirada() {
        when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoReservada));
        when(produtoPort.buscarPorId(10L)).thenReturn(Optional.of(produto));
        when(locacaoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(produtoPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Locacao resultado = service.executar(1L);

        assertThat(resultado.getStatus()).isEqualTo(StatusLocacao.RETIRADO);
        assertThat(resultado.getRetiradaAt()).isNotNull();
        verify(produtoPort).salvar(argThat(p -> !p.isDisponivel()));
    }

    @Test
    @DisplayName("deve lancar BusinessException ao retirar locacao nao RESERVADA")
    void deveLancarExcecaoSeNaoReservada() {
        locacaoReservada.setStatus(StatusLocacao.RETIRADO);
        when(locacaoPort.buscarPorId(1L)).thenReturn(Optional.of(locacaoReservada));

        assertThatThrownBy(() -> service.executar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("RESERVADO");
    }

    @Test
    @DisplayName("deve lancar EntityNotFoundException quando locacao nao existe")
    void deveLancarEntityNotFound() {
        when(locacaoPort.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.executar(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }
}
