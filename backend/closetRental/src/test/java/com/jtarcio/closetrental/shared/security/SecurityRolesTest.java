package com.jtarcio.closetrental.shared.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de controle de acesso por role.
 * Verifica que as regras de autorização estão corretamente configuradas.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@DisplayName("Segurança — controle de acesso por role")
class SecurityRolesTest {

    @Autowired WebApplicationContext context;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ─────────────────────────── Sem autenticação ───────────────────────────

    @Nested
    @DisplayName("sem autenticacao")
    class SemAutenticacao {

        @Test
        @DisplayName("GET /v1/catalogo deve ser publico")
        void catalogoGetPublico() throws Exception {
            mockMvc.perform(get("/v1/catalogo"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /v1/catalogo deve retornar 401 sem token")
        void catalogoPostSemToken() throws Exception {
            mockMvc.perform(post("/v1/catalogo")
                            .contentType("application/json")
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /v1/clientes deve retornar 401 sem token")
        void clientesSemToken() throws Exception {
            mockMvc.perform(get("/v1/clientes"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /v1/pagamentos deve retornar 401 sem token")
        void pagamentosSemToken() throws Exception {
            mockMvc.perform(get("/v1/pagamentos"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────── ROLE_CLIENTE ───────────────────────────

    @Nested
    @DisplayName("ROLE_CLIENTE")
    class RoleCliente {

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("pode acessar GET /v1/catalogo")
        void clientePodeVerCatalogo() throws Exception {
            mockMvc.perform(get("/v1/catalogo"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("nao pode DELETE /v1/catalogo — 403")
        void clienteNaoPodeDeletarCatalogo() throws Exception {
            mockMvc.perform(delete("/v1/catalogo/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("nao pode GET /v1/clientes — 403")
        void clienteNaoPodeVerClientes() throws Exception {
            mockMvc.perform(get("/v1/clientes"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("nao pode GET /v1/estoque — 403")
        void clienteNaoPodeVerEstoque() throws Exception {
            mockMvc.perform(get("/v1/estoque/historico"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("nao pode estornar pagamento — 403")
        void clienteNaoPodeEstornar() throws Exception {
            mockMvc.perform(post("/v1/pagamentos/1/estornar")
                            .contentType("application/json")
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }
    }

    // ─────────────────────────── ROLE_ATENDENTE ───────────────────────────

    @Nested
    @DisplayName("ROLE_ATENDENTE")
    class RoleAtendente {

        @Test
        @WithMockUser(roles = "ATENDENTE")
        @DisplayName("pode GET /v1/clientes")
        void atendentePodeVerClientes() throws Exception {
            mockMvc.perform(get("/v1/clientes"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ATENDENTE")
        @DisplayName("nao pode DELETE /v1/catalogo — 403")
        void atendenteNaoPodeDeletarCatalogo() throws Exception {
            mockMvc.perform(delete("/v1/catalogo/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ATENDENTE")
        @DisplayName("nao pode estornar pagamento — 403")
        void atendenteNaoPodeEstornar() throws Exception {
            mockMvc.perform(post("/v1/pagamentos/1/estornar")
                            .contentType("application/json")
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }
    }

    // ─────────────────────────── ROLE_ADMIN ───────────────────────────

    @Nested
    @DisplayName("ROLE_ADMIN")
    class RoleAdmin {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("pode GET /v1/clientes")
        void adminPodeVerClientes() throws Exception {
            mockMvc.perform(get("/v1/clientes"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("pode GET /v1/estoque/historico")
        void adminPodeVerEstoque() throws Exception {
            mockMvc.perform(get("/v1/estoque/historico"))
                    .andExpect(status().isOk());
        }
    }
}
