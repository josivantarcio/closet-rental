package com.jtarcio.closetrental.modules.pagamentos;

import com.jtarcio.closetrental.modules.pagamentos.application.PagamentoQueryService;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.MetodoPagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.Pagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.StatusPagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.ports.PagamentoRepositoryPort;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PagamentoQueryService")
class PagamentoQueryServiceTest {

    @Mock private PagamentoRepositoryPort pagamentoPort;

    @InjectMocks
    private PagamentoQueryService service;

    private Pagamento pagamento;

    @BeforeEach
    void setUp() {
        pagamento = new Pagamento();
        pagamento.setId(1L);
        pagamento.setLocacaoId(10L);
        pagamento.setValor(new BigDecimal("150.00"));
        pagamento.setMetodo(MetodoPagamento.PIX);
        pagamento.setStatus(StatusPagamento.APROVADO);
        pagamento.setCriadoEm(LocalDateTime.now());
    }

    @Test
    @DisplayName("deve retornar pagamento existente por ID")
    void deveBuscarPorId() {
        when(pagamentoPort.buscarPorId(1L)).thenReturn(Optional.of(pagamento));

        Pagamento resultado = service.buscarPorId(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getStatus()).isEqualTo(StatusPagamento.APROVADO);
    }

    @Test
    @DisplayName("deve lancar EntityNotFoundException para pagamento inexistente")
    void deveLancarExcecaoQuandoNaoEncontrado() {
        when(pagamentoPort.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deve retornar pagina de pagamentos por locacao")
    void deveListarPorLocacao() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(pagamentoPort.listarPorLocacao(eq(10L), any()))
                .thenReturn(new PageImpl<>(List.of(pagamento)));

        Page<Pagamento> resultado = service.listarPorLocacao(10L, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getLocacaoId()).isEqualTo(10L);
    }
}
