package com.jtarcio.closetrental.modules.clientes;

import com.jtarcio.closetrental.modules.clientes.application.ClienteService;
import com.jtarcio.closetrental.modules.clientes.domain.model.Cliente;
import com.jtarcio.closetrental.modules.clientes.domain.ports.ClienteRepositoryPort;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do ClienteService — cobertura alvo ≥ 80%.
 * Usa Mockito para isolar o service do repositório.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteService")
class ClienteServiceTest {

    @Mock
    private ClienteRepositoryPort repositoryPort;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteValido;

    @BeforeEach
    void setUp() {
        clienteValido = new Cliente(
                null, "Maria Silva", "maria@email.com", "11987654321",
                "12345678901", "Rua A, 100", true,
                "system", LocalDateTime.now(), null
        );
    }

    // ─────────────────────────── CRIAR ───────────────────────────

    @Nested
    @DisplayName("criar()")
    class Criar {

        @Test
        @DisplayName("deve criar cliente com dados válidos")
        void deveCriarClienteComDadosValidos() {
            when(repositoryPort.existePorEmail(anyString())).thenReturn(false);
            when(repositoryPort.existePorDocumento(anyString())).thenReturn(false);
            when(repositoryPort.salvar(any(Cliente.class))).thenAnswer(inv -> {
                Cliente c = inv.getArgument(0);
                c.setId(1L);
                return c;
            });

            Cliente resultado = clienteService.criar(clienteValido);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.isAtivo()).isTrue();
            verify(repositoryPort).salvar(clienteValido);
        }

        @Test
        @DisplayName("deve lançar ConflictException quando email já existe")
        void deveLancarConflictQuandoEmailJaExiste() {
            when(repositoryPort.existePorEmail("maria@email.com")).thenReturn(true);

            assertThatThrownBy(() -> clienteService.criar(clienteValido))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("maria@email.com");

            verify(repositoryPort, never()).salvar(any());
        }

        @Test
        @DisplayName("deve lançar ConflictException quando documento já existe")
        void deveLancarConflictQuandoDocumentoJaExiste() {
            when(repositoryPort.existePorEmail(anyString())).thenReturn(false);
            when(repositoryPort.existePorDocumento("12345678901")).thenReturn(true);

            assertThatThrownBy(() -> clienteService.criar(clienteValido))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("12345678901");

            verify(repositoryPort, never()).salvar(any());
        }

        @Test
        @DisplayName("deve garantir que cliente criado tem ativo=true")
        void deveGarantirAtivoTrue() {
            clienteValido.setAtivo(false); // mesmo que chegue false, deve ser true
            when(repositoryPort.existePorEmail(anyString())).thenReturn(false);
            when(repositoryPort.existePorDocumento(anyString())).thenReturn(false);
            when(repositoryPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Cliente resultado = clienteService.criar(clienteValido);

            assertThat(resultado.isAtivo()).isTrue();
        }
    }

    // ─────────────────────────── BUSCAR ───────────────────────────

    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar cliente quando encontrado")
        void deveRetornarClienteQuandoEncontrado() {
            clienteValido.setId(1L);
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(clienteValido));

            Cliente resultado = clienteService.buscarPorId(1L);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNome()).isEqualTo("Maria Silva");
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException quando não encontrado")
        void deveLancarEntityNotFoundQuandoNaoEncontrado() {
            when(repositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clienteService.buscarPorId(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ─────────────────────────── LISTAR ───────────────────────────

    @Nested
    @DisplayName("listar()")
    class Listar {

        @Test
        @DisplayName("deve retornar página de clientes")
        void deveRetornarPaginaDeClientes() {
            clienteValido.setId(1L);
            Page<Cliente> page = new PageImpl<>(List.of(clienteValido));
            when(repositoryPort.listarTodos(any())).thenReturn(page);

            Page<Cliente> resultado = clienteService.listar(PageRequest.of(0, 20));

            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).getNome()).isEqualTo("Maria Silva");
        }
    }

    // ─────────────────────────── ATUALIZAR ───────────────────────────

    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("deve atualizar campos permitidos")
        void deveAtualizarCamposPermitidos() {
            clienteValido.setId(1L);
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(clienteValido));
            when(repositoryPort.existePorEmailEIdDiferente(anyString(), eq(1L))).thenReturn(false);
            when(repositoryPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Cliente atualizado = new Cliente();
            atualizado.setNome("Maria Souza");
            atualizado.setEmail("maria@email.com");
            atualizado.setTelefone("11999990000");
            atualizado.setDocumento("12345678901"); // mesmo documento
            atualizado.setEndereco("Rua B, 200");

            Cliente resultado = clienteService.atualizar(1L, atualizado);

            assertThat(resultado.getNome()).isEqualTo("Maria Souza");
            assertThat(resultado.getTelefone()).isEqualTo("11999990000");
            assertThat(resultado.getEndereco()).isEqualTo("Rua B, 200");
        }

        @Test
        @DisplayName("deve lançar BusinessException ao tentar trocar documento")
        void deveLancarExcecaoAoTrocarDocumento() {
            clienteValido.setId(1L);
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(clienteValido));
            when(repositoryPort.existePorEmailEIdDiferente(anyString(), eq(1L))).thenReturn(false);

            Cliente comDocumentoDiferente = new Cliente();
            comDocumentoDiferente.setNome("Maria");
            comDocumentoDiferente.setEmail("maria@email.com");
            comDocumentoDiferente.setTelefone("11999990000");
            comDocumentoDiferente.setDocumento("99988877766"); // diferente!

            assertThatThrownBy(() -> clienteService.atualizar(1L, comDocumentoDiferente))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("documento");
        }

        @Test
        @DisplayName("deve lançar ConflictException quando novo email já pertence a outro cliente")
        void deveLancarConflictQuandoEmailEmUso() {
            clienteValido.setId(1L);
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(clienteValido));
            when(repositoryPort.existePorEmailEIdDiferente("outro@email.com", 1L)).thenReturn(true);

            Cliente comEmailDuplicado = new Cliente();
            comEmailDuplicado.setEmail("outro@email.com");
            comEmailDuplicado.setDocumento("12345678901");

            assertThatThrownBy(() -> clienteService.atualizar(1L, comEmailDuplicado))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("outro@email.com");
        }
    }

    // ─────────────────────────── DELETAR ───────────────────────────

    @Nested
    @DisplayName("deletar()")
    class Deletar {

        @Test
        @DisplayName("deve deletar cliente sem locações ativas")
        void deveDeletarClienteSemLocacoesAtivas() {
            clienteValido.setId(1L);
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(clienteValido));
            when(repositoryPort.possuiLocacaoAtiva(1L)).thenReturn(false);

            assertThatCode(() -> clienteService.deletar(1L)).doesNotThrowAnyException();

            verify(repositoryPort).deletar(1L);
        }

        @Test
        @DisplayName("deve lançar BusinessException ao deletar cliente com locação ativa")
        void deveLancarExcecaoAoDeletarClienteComLocacaoAtiva() {
            clienteValido.setId(1L);
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(clienteValido));
            when(repositoryPort.possuiLocacaoAtiva(1L)).thenReturn(true);

            assertThatThrownBy(() -> clienteService.deletar(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("locações ativas");

            verify(repositoryPort, never()).deletar(any());
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException ao deletar cliente inexistente")
        void deveLancarEntityNotFoundAoDeletarClienteInexistente() {
            when(repositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clienteService.deletar(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ─────────────────────────── INATIVAR / REATIVAR ───────────────────────────

    @Nested
    @DisplayName("inativar() e reativar()")
    class InativarReativar {

        @Test
        @DisplayName("deve inativar cliente ativo")
        void deveInativarClienteAtivo() {
            clienteValido.setId(1L);
            clienteValido.setAtivo(true);
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(clienteValido));
            when(repositoryPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Cliente resultado = clienteService.inativar(1L);

            assertThat(resultado.isAtivo()).isFalse();
        }

        @Test
        @DisplayName("deve lançar BusinessException ao inativar cliente já inativo")
        void deveLancarExcecaoAoInativarClienteJaInativo() {
            clienteValido.setId(1L);
            clienteValido.setAtivo(false);
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(clienteValido));

            assertThatThrownBy(() -> clienteService.inativar(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("já está inativo");
        }

        @Test
        @DisplayName("deve reativar cliente inativo")
        void deveReativarClienteInativo() {
            clienteValido.setId(1L);
            clienteValido.setAtivo(false);
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(clienteValido));
            when(repositoryPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

            Cliente resultado = clienteService.reativar(1L);

            assertThat(resultado.isAtivo()).isTrue();
        }

        @Test
        @DisplayName("deve lançar BusinessException ao reativar cliente já ativo")
        void deveLancarExcecaoAoReativarClienteJaAtivo() {
            clienteValido.setId(1L);
            clienteValido.setAtivo(true);
            when(repositoryPort.buscarPorId(1L)).thenReturn(Optional.of(clienteValido));

            assertThatThrownBy(() -> clienteService.reativar(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("já está ativo");
        }
    }

    // ─────────────────────── DOMAIN MODEL ───────────────────────────

    @Nested
    @DisplayName("Cliente domain model")
    class ClienteDomain {

        @Test
        @DisplayName("documentoSemFormatacao deve remover pontuação do CPF")
        void deveRemoverPontuacaoCpf() {
            Cliente c = new Cliente();
            c.setDocumento("123.456.789-01");

            assertThat(c.documentoSemFormatacao()).isEqualTo("12345678901");
        }

        @Test
        @DisplayName("documentoSemFormatacao deve remover pontuação do CNPJ")
        void deveRemoverPontuacaoCnpj() {
            Cliente c = new Cliente();
            c.setDocumento("12.345.678/0001-99");

            assertThat(c.documentoSemFormatacao()).isEqualTo("12345678000199");
        }

        @Test
        @DisplayName("documentoSemFormatacao deve retornar null quando documento é null")
        void deveRetornarNullQuandoDocumentoNulo() {
            Cliente c = new Cliente();
            c.setDocumento(null);

            assertThat(c.documentoSemFormatacao()).isNull();
        }

        @Test
        @DisplayName("inativar() deve setar ativo=false")
        void inativarDeveSetarFalse() {
            Cliente c = new Cliente();
            c.setAtivo(true);
            c.inativar();
            assertThat(c.isAtivo()).isFalse();
        }

        @Test
        @DisplayName("reativar() deve setar ativo=true")
        void reativarDeveSetarTrue() {
            Cliente c = new Cliente();
            c.setAtivo(false);
            c.reativar();
            assertThat(c.isAtivo()).isTrue();
        }
    }
}
