# CHECKLIST COMPLETO — Sistema Closet Rental (Atelier)
> Loja de Aluguel de Roupas & Vestidos | Última atualização: 2026-02-27
> Progresso global: ~80% (Backend) | Frontend: 0%

---

## LEGENDA
- `[ ]` Pendente  |  `[x]` Concluído  |  `[-]` Parcial / Débito  |  `[!]` Atenção / Débito Técnico

---

## MILESTONE 0 — Setup & Infraestrutura Base
**Status: CONCLUÍDO — v0.1.0**

- [x] Repositório git + estrutura de pastas (`backend/`, `frontend/`)
- [x] `pom.xml` — Java 21, Spring Boot 4.0.0, JPA, Security, Validation, Web MVC
- [x] MapStruct 1.6.3, Springdoc OpenAPI 2.7.0, Lombok, PostgreSQL, H2 (test)
- [x] `maven-compiler-plugin` com `annotationProcessorPaths` (Lombok + MapStruct)
- [x] `application.yml` — datasource PostgreSQL (`aluguel`, porta 5432)
- [x] `application-test.yml` — H2 in-memory para testes
- [x] `ClosetRentalApplication.java` com `@EnableJpaAuditing(auditorAwareRef = "auditorAware")`
- [x] `OpenApiConfig.java` — Swagger + Bearer JWT scheme
- [x] `SecurityConfig.java` — stateless JWT, roles, entry point 401
- [x] `GlobalExceptionHandler.java` — Business, NotFound, Conflict, Validation, Auth
- [x] `BusinessException`, `ConflictException`, `EntityNotFoundException`
- [!] **DÉBITO:** `ddl-auto: update` — migrar para Flyway + `validate` antes de produção
- [!] **DÉBITO:** `show-sql: true` — desativar em produção
- [ ] Configurar Flyway: `V1__create_tables.sql`
- [ ] Configurar profile `dev` vs `prod`
- [ ] Adicionar Spring Actuator para health-check
- [ ] Configurar CORS para o frontend

---

## MILESTONE 1 — Módulo Catálogo
**Status: CONCLUÍDO — v0.2.0**

- [x] `Produto.java` — domain POJO, `marcarDisponivel()`, `marcarIndisponivel()`, `calcularValor()`
- [x] `ProdutoRepositoryPort.java` — interface com todos os métodos
- [x] `ProdutoEntity.java` — @Entity com auditoria (`@CreatedBy`, `@CreatedDate`)
- [x] `ProdutoRepositoryJpa.java` — Spring Data JPA
- [x] `ProdutoRepositoryAdapter.java`
- [x] `ProdutoMapper.java` (MapStruct)
- [x] `ProdutoService.java` — CRUD + `buscarDisponiveisNoPeriodo` (NOT EXISTS, O(log n))
- [x] `ProdutoRequest.java` / `ProdutoResponse.java`
- [x] `CatalogoController.java` — `/v1/catalogo` com paginação + OpenAPI completo
- [!] **DÉBITO:** Produto sem campos: `tamanho`, `cor`, `descricao`, `imagemUrl`
- [!] **DÉBITO:** Sem índices explícitos: `idx_produtos_disponivel`, `idx_produtos_categoria`
- [ ] Testes unitários `ProdutoServiceTest` (cobertura ≥ 80%)
- [ ] Filtro por categoria e disponibilidade nos query params

---

## MILESTONE 2 — Módulo Clientes
**Status: CONCLUÍDO — v0.3.0**

- [x] `Cliente.java` — domain POJO: `id, nome, email, telefone, documento, ativo`
- [x] `ClienteRepositoryPort.java` — salvar, buscar, listar, deletar, existsPor...
- [x] `ClienteEntity.java` — @Entity com `@UniqueConstraint(documento, email)`
- [x] `ClienteRepositoryJpa.java`
- [x] `ClienteRepositoryAdapter.java`
- [x] `ClienteMapper.java` (MapStruct)
- [x] `ClienteService.java` — CRUD + inativar/reativar + validação unique
- [x] `ClienteRequest.java` / `ClienteResponse.java`
- [x] `ClienteController.java` — `/v1/clientes` + inativar/reativar + OpenAPI
- [x] Testes: domain model, buscarPorId, atualizar, deletar, inativar/reativar

---

## MILESTONE 3 — Módulo Locações (CORE)
**Status: CONCLUÍDO — v0.4.0**

- [x] `Locacao.java` — domain POJO: id, clienteId, produtoId, datas, status, valores, multa
- [x] `StatusLocacao.java` — enum: `RESERVADO, RETIRADO, DEVOLVIDO, CANCELADO`
- [x] `LocacaoRepositoryPort.java`
- [x] `LocacaoEntity.java` — @Index em produto+período, cliente, status
- [x] `LocacaoRepositoryJpa.java` — query overlap com NOT EXISTS (O(log n))
- [x] `LocacaoRepositoryAdapter.java`
- [x] `LocacaoMapper.java` (MapStruct)
- [x] `CriarReservaService.java` — validação overlap, cálculo valorTotal + caução
- [x] `RegistrarRetiradaService.java` — valida RESERVADO, marca produto indisponível
- [x] `RegistrarDevolucaoService.java` — calcula multa por atraso, marca disponível
- [x] `CancelarReservaService.java` — apenas RESERVADO
- [x] `LocacaoQueryService.java` — filtros por cliente, status, listagem
- [x] `LocacaoController.java` — `/v1/locacoes` com 6 endpoints + OpenAPI
- [x] 44 testes: CriarReserva, Retirada, Devolução (com multa), Cancelamento, Query

---

## MILESTONE 4 — Módulo Estoque
**Status: CONCLUÍDO — v0.5.0**

- [x] `MovimentacaoEstoque.java` — domain: id, produtoId, tipo, motivo, locacaoId, operador
- [x] `TipoMovimentacao.java` — enum: `SAIDA, ENTRADA, MANUTENCAO, RETORNO_MANUTENCAO`
- [x] `EstoqueRepositoryPort.java`
- [x] `MovimentacaoEstoqueEntity.java` — @Entity `movimentacoes_estoque` + @Index
- [x] `MovimentacaoEstoqueRepositoryJpa.java`
- [x] `EstoqueRepositoryAdapter.java`
- [x] `MovimentacaoEstoqueMapper.java` (MapStruct)
- [x] `EstoqueService.java` — registrarSaida/Entrada, manutenção, histórico, disponibilidade
- [x] `EstoqueController.java` — `/v1/estoque` com 5 endpoints + OpenAPI
- [x] Testes: disponibilidade, saída, entrada, manutenção, histórico

---

## MILESTONE 5 — Módulo Pagamentos
**Status: CONCLUÍDO — v0.6.0**

- [x] `Pagamento.java` — domain: id, locacaoId, valor, status, metodo, motivo, datas, createdBy
- [x] `StatusPagamento.java` — enum: `PENDENTE, APROVADO, FALHA, ESTORNADO`
- [x] `MetodoPagamento.java` — enum: `PIX, CARTAO_CREDITO, CARTAO_DEBITO, DINHEIRO`
- [x] `PagamentoRepositoryPort.java`
- [x] `GatewayPagamentoPort.java` + record `PagamentoResultado`
- [x] `PagamentoEntity.java` — 4 índices (locacao, status, metodo, criado_em)
- [x] `PagamentoRepositoryJpa.java`
- [x] `PagamentoRepositoryAdapter.java`
- [x] `PagamentoMapper.java` (MapStruct)
- [x] `GatewayPagamentoMock.java` — PIX/DINHEIRO sempre aprovado; Cartão 10% falha
- [x] `ProcessarPagamentoService.java` — valida locação, chama gateway, persiste resultado
- [x] `EstornarPagamentoService.java` — apenas APROVADO
- [x] `PagamentoQueryService.java` — busca por id, locação, status, todos
- [x] `PagamentoController.java` — `/v1/pagamentos` com 4 endpoints + OpenAPI
- [x] 18 testes: processar (10), estornar (5), query (3)

---

## MILESTONE 6 — Segurança JWT
**Status: CONCLUÍDO — v0.7.0**

- [x] jjwt 0.12.6 adicionado ao `pom.xml`; asciidoctor-plugin removido (débito quitado)
- [x] `JwtService.java` — geração + validação HS256, 24h, jjwt 0.12.6
- [x] `JwtAuthenticationFilter.java` — `OncePerRequestFilter`, popula SecurityContext
- [x] `JwtAuditorAware.java` — `@Component("auditorAware")`, lê nome do SecurityContext
- [x] `InMemoryUserDetailsConfig.java` — UserDetailsService + PasswordEncoder separados (evita circular dependency)
- [x] `SecurityConfig.java` — reescrito stateless, JWT filter, entry point 401
- [x] `OpenApiConfig.java` — Bearer JWT scheme no Swagger
- [x] `AuthController.java` — `POST /v1/auth/login` + `LoginRequest` / `LoginResponse`
- [x] `GlobalExceptionHandler` — handler `AuthenticationException` → 401
- [x] `AuthControllerTest` — 5 testes (login admin/atendente, 401, campos em branco)
- [x] `SecurityRolesTest` — 14 testes (@WithMockUser CLIENTE/ATENDENTE/ADMIN)
- [!] **DÉBITO:** Autenticação in-memory — migrar para tabela `usuarios` no banco
- [ ] `POST /v1/auth/refresh` — refresh token
- [ ] Rate limiting por IP (Bucket4j)
- [ ] Conformidade LGPD: mascaramento de CPF nos logs

---

## MILESTONE 7 — Documentação
**Status: CONCLUÍDO — v0.8.0**

- [x] `README.md` — stack, pré-requisitos, DB, execução, auth JWT, todos os endpoints, curl completo, arquitetura, variáveis, débitos
- [x] Swagger UI enriquecido — `@Operation` e `@ApiResponses` em todos os controllers
- [x] `springdoc` configurado — tags-sorter, operations-sorter, display-request-duration, try-it-out-enabled
- [x] Wiki GitHub — 6 páginas: Home, Arquitetura, Autenticação, Módulos, Regras de Negócio, Configuração, Guia de Desenvolvimento

---

## PENDENTE — Próximas etapas (em ordem de prioridade)

### Backend — Qualidade & Produção

- [ ] **Flyway** — `V1__create_tables.sql`, `V2__add_indexes.sql`, `V3__add_constraints.sql`; mudar `ddl-auto` para `validate`
- [ ] **Testes unitários faltantes** — `ProdutoServiceTest` (cobertura ≥ 80%)
- [ ] **Jacoco** — relatório de cobertura HTML: `mvn verify`; meta ≥ 80% em `application/` e `domain/`
- [ ] **Testcontainers** — testes de integração com PostgreSQL real; validar queries de overlap
- [ ] **Spring Actuator** — health-check endpoint
- [ ] **CORS** — configurar para o futuro frontend
- [ ] **Campos faltantes no Produto** — `tamanho`, `cor`, `descricao`, `imagemUrl`
- [ ] **Índices explícitos** — `idx_produtos_disponivel`, `idx_produtos_categoria`
- [ ] **Refresh token** — `POST /v1/auth/refresh`
- [ ] **Tabela de usuários** — substituir in-memory por persistência real

### Backend — Módulo Relatórios (RF do documento original)

- [ ] `GET /v1/relatorios/disponibilidade?data=` — peças disponíveis em data
- [ ] `GET /v1/relatorios/historico-cliente/{id}` — histórico de locações do cliente
- [ ] `GET /v1/relatorios/faturamento?mes=&ano=` — receita no período
- [ ] `GET /v1/relatorios/pecas-mais-alugadas` — ranking de produtos
- [ ] `GET /v1/relatorios/atrasos-pendentes` — locações em atraso

### Backend — Notificações (RF09 do documento original)

- [ ] `NotificacaoService.java` — interface com `enviarEmail()` / `enviarWhatsApp()`
- [ ] `EmailNotificacaoAdapter.java` — Spring Mail (SMTP)
- [ ] Confirmação de reserva, lembrete de retirada/devolução, aviso de atraso

### DevOps

- [ ] `docker-compose.yml` — PostgreSQL + backend (dev)
- [ ] `Dockerfile` backend — multi-stage build (Java 21 + Maven)
- [ ] GitHub Actions CI — build + tests + coverage report
- [ ] Variáveis de ambiente seguras (`.env.example`)

### Frontend — Next.js 14+ (0% concluído)

**Stack:** Next.js 14+ (App Router) + TypeScript + Tailwind CSS + shadcn/ui + Framer Motion
**Tema:** Elegância sombria — Deep Rose `#c2185b`, Onyx `#1a1a2e`, Gold `#d4af37`, Ivory `#faf8f5`
**Tipografia:** Playfair Display (headings) + Inter (body)

- [ ] Setup Next.js + Tailwind + shadcn/ui + Zustand/TanStack Query + ESLint/Prettier
- [ ] Design System: `ProductCard`, `StatusBadge`, `DateRangePicker`, `Timeline`, `PriceDisplay`
- [ ] `/login` — autenticação com imagem de fundo (moda)
- [ ] `/dashboard` — KPIs: locações hoje, em atraso, receita do mês, peças disponíveis
- [ ] `/catalogo` — grid de produtos com filtros (categoria, disponibilidade, faixa de preço)
- [ ] `/clientes` — tabela com busca e paginação
- [ ] `/locacoes` — lista com filtros de status + fluxo de reserva em steps
- [ ] `/locacoes/[id]` — timeline de estados + botões de ação contextual
- [ ] `/estoque` — visão geral + histórico de movimentações
- [ ] `/relatorios` — gráficos (Recharts): faturamento, peças mais alugadas
- [ ] Portal do Cliente — `/portal/minhas-locacoes`
- [ ] Testes: Vitest + Testing Library (componentes) + Playwright (E2E)

---

## RESUMO DO PROGRESSO

| Módulo | Backend | Testes | Frontend |
|---|---|---|---|
| Setup/Config | ✅ 100% | ✅ (security) | ❌ 0% |
| Catálogo | ✅ 100% | ⚠️ parcial | ❌ 0% |
| Clientes | ✅ 100% | ✅ | ❌ 0% |
| Locações | ✅ 100% | ✅ 44 testes | ❌ 0% |
| Estoque | ✅ 100% | ✅ | ❌ 0% |
| Pagamentos | ✅ 100% | ✅ 18 testes | ❌ 0% |
| Segurança JWT | ✅ 100% | ✅ 19 testes | ❌ 0% |
| Documentação | ✅ 100% | — | — |
| Relatórios | ❌ 0% | ❌ 0% | ❌ 0% |
| Flyway | ❌ 0% | — | — |
| Notificações | ❌ 0% | ❌ 0% | — |
| DevOps | ❌ 0% | — | — |
| Frontend | — | — | ❌ 0% |

**Total de testes:** 119 | **BUILD SUCCESS** | **Releases:** v0.1.0 → v0.8.0

**Próximas etapas recomendadas (backend):**
1. Flyway (débito técnico crítico antes de produção)
2. Módulo Relatórios (RF do documento original)
3. Docker Compose + CI/CD

**Próxima etapa recomendada (grande):**
- Frontend Next.js — toda a interface ainda está em branco
