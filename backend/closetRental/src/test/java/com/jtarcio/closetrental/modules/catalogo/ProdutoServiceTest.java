package com.jtarcio.closetrental.modules.catalogo;

import com.jtarcio.closetrental.modules.catalogo.application.ProdutoService;
import com.jtarcio.closetrental.modules.catalogo.domain.model.Produto;
import com.jtarcio.closetrental.modules.catalogo.domain.ports.ProdutoRepositoryPort;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do ProdutoService — cobertura alvo ≥ 80%.
 * Usa Mockito para isolar o service do repositório.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoService")
class ProdutoServiceTest {

    @Mock
    private ProdutoRepositoryPort repositoryPort;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produtoValido;

    @BeforeEach
    void setUp() {
        produtoValido = new Produto(
                null, "Vestido Longo Azul", "VESTIDO",
                new BigDecimal("150.00"), "VES-001", true,
                "P", "Azul", "Vestido para festas formais", null,
                "system", LocalDateTime.now()
        );
    }

    // ── criar ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("criar()")
    class Criar {

        @Test
        @DisplayName("deve criar produto com dados válidos")
        void deveCriarProdutoValido() {
            Produto salvo = new Produto(
                    1L, produtoValido.getNome(), produtoValido.getCategoria(),
                    produtoValido.getValorDiaria(), produtoValido.getCodigo(),
                    produtoValido.isDisponivel(), produtoValido.getTamanho(),
                    produtoValido.getCor(), produtoValido.getDescricao(),
                    produtoValido.getImagemUrl(), "system", LocalDateTime.now()
            );
            when(repositoryPort.salvar(any())).thenReturn(salvo);

            Produto resultado = produtoService.criar(produtoValido);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNome()).isEqualTo("Vestido Longo Azul");
            assertThat(resultado.getTamanho()).isEqualTo("P");
            assertThat(resultado.getCor()).isEqualTo("Azul");
            verify(repositoryPort, times(1)).salvar(produtoValido);
        }

        @Test
        @DisplayName("deve criar produto com valorDiaria zero (valor mínimo permitido)")
        void deveCriarProdutoComValorZero() {
            produtoValido.setValorDiaria(BigDecimal.ZERO);
            when(repositoryPort.salvar(any())).thenReturn(produtoValido);

            assertThatCode(() -> produtoService.criar(produtoValido))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deve lançar BusinessException para valorDiaria negativo")
        void deveLancarExcecaoParaValorNegativo() {
            produtoValido.setValorDiaria(new BigDecimal("-1.00"));

            assertThatThrownBy(() -> produtoService.criar(produtoValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("negativo");

            verify(repositoryPort, never()).salvar(any());
        }

        @Test
        @DisplayName("deve aceitar valorDiaria nulo sem lançar exceção")
        void deveAceitarValorNulo() {
            produtoValido.setValorDiaria(null);
            when(repositoryPort.salvar(any())).thenReturn(produtoValido);

            assertThatCode(() -> produtoService.criar(produtoValido))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deve criar produto sem campos opcionais (tamanho, cor, descricao, imagemUrl)")
        void deveCriarProdutoSemCamposOpcionais() {
            Produto simples = new Produto(
                    null, "Terno Preto", "TERNO",
                    new BigDecimal("200.00"), null, true,
                    null, null, null, null, "admin", LocalDateTime.now()
            );
            when(repositoryPort.salvar(any())).thenReturn(simples);

            Produto resultado = produtoService.criar(simples);

            assertThat(resultado.getTamanho()).isNull();
            assertThat(resultado.getCor()).isNull();
        }
    }

    // ── listar ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("listar()")
    class Listar {

        @Test
        @DisplayName("deve retornar página de produtos")
        void deveRetornarPaginaDeProdutos() {
            PageRequest pageable = PageRequest.of(0, 10);
            Page<Produto> pagina = new PageImpl<>(List.of(produtoValido));
            when(repositoryPort.listarTodos(any(), eq(pageable))).thenReturn(pagina);

            Page<Produto> resultado = produtoService.listar(pageable);

            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).getNome()).isEqualTo("Vestido Longo Azul");
            verify(repositoryPort, times(1)).listarTodos(any(), eq(pageable));
        }

        @Test
        @DisplayName("deve retornar página vazia quando não há produtos")
        void deveRetornarPaginaVazia() {
            PageRequest pageable = PageRequest.of(0, 10);
            when(repositoryPort.listarTodos(any(), eq(pageable))).thenReturn(Page.empty());

            Page<Produto> resultado = produtoService.listar(pageable);

            assertThat(resultado.getContent()).isEmpty();
        }
    }

    // ── buscarPorId ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar produto quando encontrado")
        void deveRetornarProdutoEncontrado() {
            produtoValido.setId(1L);
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(produtoValido));

            Produto resultado = produtoService.buscarPorId(1L);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNome()).isEqualTo("Vestido Longo Azul");
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException quando não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(repositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> produtoService.buscarPorId(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ── atualizar ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("deve atualizar todos os campos do produto")
        void deveAtualizarTodosOsCampos() {
            produtoValido.setId(1L);
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(produtoValido));

            Produto atualizacao = new Produto(
                    null, "Vestido Curto Rosa", "VESTIDO",
                    new BigDecimal("120.00"), "VES-002", false,
                    "M", "Rosa", "Vestido casual", "https://img.com/foto.jpg",
                    null, null
            );
            Produto salvo = new Produto(
                    1L, "Vestido Curto Rosa", "VESTIDO",
                    new BigDecimal("120.00"), "VES-002", false,
                    "M", "Rosa", "Vestido casual", "https://img.com/foto.jpg",
                    "admin", LocalDateTime.now()
            );
            when(repositoryPort.salvar(any())).thenReturn(salvo);

            Produto resultado = produtoService.atualizar(1L, atualizacao);

            assertThat(resultado.getNome()).isEqualTo("Vestido Curto Rosa");
            assertThat(resultado.getTamanho()).isEqualTo("M");
            assertThat(resultado.getCor()).isEqualTo("Rosa");
            assertThat(resultado.getDescricao()).isEqualTo("Vestido casual");
            assertThat(resultado.getImagemUrl()).isEqualTo("https://img.com/foto.jpg");
            assertThat(resultado.isDisponivel()).isFalse();
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException ao atualizar produto inexistente")
        void deveLancarExcecaoAtualizarInexistente() {
            when(repositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> produtoService.atualizar(99L, produtoValido))
                    .isInstanceOf(EntityNotFoundException.class);
            verify(repositoryPort, never()).salvar(any());
        }

        @Test
        @DisplayName("deve lançar BusinessException ao atualizar com valorDiaria negativo")
        void deveLancarExcecaoAtualizarValorNegativo() {
            produtoValido.setId(1L);
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(produtoValido));

            Produto atualizacao = new Produto(
                    null, "Vestido", "VESTIDO", new BigDecimal("-5.00"),
                    null, true, null, null, null, null, null, null
            );

            assertThatThrownBy(() -> produtoService.atualizar(1L, atualizacao))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("negativo");
            verify(repositoryPort, never()).salvar(any());
        }
    }

    // ── deletar ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deletar()")
    class Deletar {

        @Test
        @DisplayName("deve deletar produto sem locações ativas")
        void deveDeletarProdutoSemLocacoes() {
            produtoValido.setId(1L);
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(produtoValido));
            when(repositoryPort.existeEmLocacaoAtiva(1L)).thenReturn(false);

            assertThatCode(() -> produtoService.deletar(1L))
                    .doesNotThrowAnyException();

            verify(repositoryPort, times(1)).deletar(1L);
        }

        @Test
        @DisplayName("deve lançar BusinessException ao deletar produto com locação ativa")
        void deveLancarExcecaoAoDeletarComLocacaoAtiva() {
            produtoValido.setId(1L);
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(produtoValido));
            when(repositoryPort.existeEmLocacaoAtiva(1L)).thenReturn(true);

            assertThatThrownBy(() -> produtoService.deletar(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("locação ativa");

            verify(repositoryPort, never()).deletar(anyLong());
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException ao deletar produto inexistente")
        void deveLancarExcecaoAoDeletarInexistente() {
            when(repositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> produtoService.deletar(99L))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(repositoryPort, never()).deletar(anyLong());
            verify(repositoryPort, never()).existeEmLocacaoAtiva(anyLong());
        }
    }

    // ── calcularValor (domain) ─────────────────────────────────────────────

    @Nested
    @DisplayName("Produto.calcularValor()")
    class CalcularValor {

        @Test
        @DisplayName("deve calcular valorTotal = valorDiaria * dias + caucao")
        void deveCalcularValorTotal() {
            Produto p = new Produto(
                    1L, "Vestido", "VESTIDO", new BigDecimal("100.00"),
                    null, true, null, null, null, null, "admin", LocalDateTime.now()
            );

            BigDecimal resultado = p.calcularValor(5, new BigDecimal("200.00"));

            assertThat(resultado).isEqualByComparingTo(new BigDecimal("700.00"));
        }

        @Test
        @DisplayName("deve calcular valorTotal com caução zero")
        void deveCalcularValorSemCaucao() {
            Produto p = new Produto(
                    1L, "Vestido", "VESTIDO", new BigDecimal("80.00"),
                    null, true, null, null, null, null, "admin", LocalDateTime.now()
            );

            BigDecimal resultado = p.calcularValor(3, BigDecimal.ZERO);

            assertThat(resultado).isEqualByComparingTo(new BigDecimal("240.00"));
        }
    }

    // ── marcarDisponivel / marcarIndisponivel ──────────────────────────────

    @Nested
    @DisplayName("Produto — disponibilidade")
    class Disponibilidade {

        @Test
        @DisplayName("marcarDisponivel() deve setar disponivel = true")
        void deveMarcarDisponivel() {
            produtoValido.setDisponivel(false);
            produtoValido.marcarDisponivel();
            assertThat(produtoValido.isDisponivel()).isTrue();
        }

        @Test
        @DisplayName("marcarIndisponivel() deve setar disponivel = false")
        void deveMarcarIndisponivel() {
            produtoValido.marcarIndisponivel();
            assertThat(produtoValido.isDisponivel()).isFalse();
        }
    }
}
