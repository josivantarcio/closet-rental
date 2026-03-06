# CHECKLIST COMPLETO — Sistema Closet Rental (Atelier)
> Loja de Aluguel de Roupas & Vestidos | Última atualização: 2026-03-05 (v2.0.0-fase2)
> Progresso global: Fase 2 concluída | 156 testes backend | BUILD SUCCESS

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
- [x] `SecurityConfig.java` — stateless JWT, roles, entry point 401, CORS configurado
- [x] `GlobalExceptionHandler.java` — Business, NotFound, Conflict, Validation, Auth
- [x] `BusinessException`, `ConflictException`, `EntityNotFoundException`
- [x] **Flyway** — `V1__create_tables.sql`, `V2__add_indexes.sql`; `ddl-auto: validate` — v0.9.0
- [x] **CORS** — `CorsConfigurationSource` no SecurityConfig; origem via `CORS_ALLOWED_ORIGINS` — v1.4.0
- [x] **Variáveis de ambiente** — todos os segredos externalizados com `${VAR:default}` — v1.4.0
- [x] **Profile prod** — `application-prod.yml`: show-sql=false, Swagger desabilitado — v1.4.0
- [!] **DÉBITO:** Autenticação in-memory — migrar para tabela `usuarios` no banco
- [ ] Spring Actuator — health-check endpoint
- [ ] Rate limiting por IP (Bucket4j)

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
- [x] **Campos do Produto:** `tamanho`, `cor`, `descricao`, `imagemUrl` — Flyway V4 — v2.0-fase1
- [x] Testes unitários `ProdutoServiceTest` — 19 testes, Jacoco ≥ 80% — v2.0-fase1
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

- [x] jjwt 0.12.6 adicionado ao `pom.xml`
- [x] `JwtService.java` — geração + validação HS256, 24h, jjwt 0.12.6
- [x] `JwtAuthenticationFilter.java` — `OncePerRequestFilter`, popula SecurityContext
- [x] `JwtAuditorAware.java` — `@Component("auditorAware")`, lê nome do SecurityContext
- [x] `InMemoryUserDetailsConfig.java` — UserDetailsService + PasswordEncoder separados
- [x] `SecurityConfig.java` — stateless, JWT filter, entry point 401, CORS
- [x] `OpenApiConfig.java` — Bearer JWT scheme no Swagger
- [x] `AuthController.java` — `POST /v1/auth/login` + `LoginRequest` / `LoginResponse`
- [x] `GlobalExceptionHandler` — handler `AuthenticationException` → 401
- [x] `AuthControllerTest` — 5 testes | `SecurityRolesTest` — 14 testes
- [x] **Autenticação por banco** — tabela `usuarios`, `UserDetailsServiceImpl`, `DataInitializer` — v2.0-fase1
- [x] **Refresh Token** — `POST /v1/auth/refresh` + `POST /v1/auth/logout`, access 15min, refresh 7d — v2.0-fase1

---

## MILESTONE 7 — Documentação
**Status: CONCLUÍDO — v0.8.0**

- [x] `README.md` — stack, pré-requisitos, DB, execução, auth JWT, endpoints, arquitetura
- [x] Swagger UI enriquecido — `@Operation` e `@ApiResponses` em todos os controllers
- [x] Wiki GitHub — 6 páginas

---

## MILESTONE 8 — Frontend Next.js 16
**Status: CONCLUÍDO — v1.2.0 → v1.6.0**

**Stack:** Next.js 16 App Router + TypeScript + Tailwind CSS + shadcn/ui + Framer Motion
**Tema:** Elegância sombria — Deep Rose `#c2185b`, Onyx `#1a1a2e`, Gold `#d4af37`, Ivory `#faf8f5`

- [x] Setup Next.js + Tailwind + shadcn/ui + Zustand + TanStack Query + ESLint
- [x] Design System: `StatusBadge`, `Sidebar`, `Header`
- [x] `/login` — form JWT + hint contas de teste + Framer Motion
- [x] `/dashboard` — 4 KPIs + peças em uso + atrasos pendentes
- [x] `/catalogo` — grid cards com CRUD completo (dialog criar/editar) + paginação — v1.2.0
- [x] `/catalogo` — toggle de visualização (grid/lista) + busca global (size=200) — v1.6.0
- [x] `/clientes` — tabela com CRUD + inativar/reativar + paginação
- [x] `/clientes` — CPF/Documento exibido corretamente (LGPD: mascarado na lista, completo ao editar) — v1.6.0
- [x] `/locacoes` — lista filtrada por status (tabs) + criar reserva + ações contextuais
- [x] `/locacoes` — Combobox de busca para cliente e produto (sem digitar IDs) — v1.6.0
- [x] `/locacoes` — nomes reais de cliente/produto na tabela via O(1) Map lookup — v1.6.0
- [x] `/locacoes/[id]` — detalhe com timeline de estados + ações + pagamentos da locação
- [x] `/locacoes/[id]` — botão "Processar Pagamento" com dialog (valor pré-preenchido + método) — v1.6.0
- [x] `/estoque` — histórico de movimentações paginado + cards de resumo (total/disponíveis/indisponíveis) — v1.6.0
- [x] `/estoque` — ações rápidas: Enviar para Manutenção / Retornar da Manutenção via Combobox — v1.6.0
- [x] `/pagamentos` — cards resumo (Aprovados, Receita Total, Estornados, Falhas) — v1.6.0
- [x] `/pagamentos` — filtro por status + link para detalhe da locação + contagem de itens — v1.6.0
- [x] `/relatorios` — faturamento mensal + peças mais alugadas + atrasos pendentes
- [x] `src/components/shared/combobox.tsx` — Combobox reutilizável (Popover + busca local) — v1.6.0
- [x] `src/lib/api.ts` — axios + interceptors (Bearer JWT + redirect 401)
- [x] `src/store/auth.store.ts` — Zustand persist
- [x] `src/types/index.ts` — tipos espelho dos DTOs do backend
- [x] Build: 0 erros TypeScript | 0 warnings
- [x] `error.tsx` — Error Boundary com "Tentar novamente" + error.digest — v1.4.0
- [x] `not-found.tsx` — página 404 personalizada com navegação — v1.4.0

---

## MILESTONE 9 — Testes Frontend
**Status: CONCLUÍDO — v1.3.0 / v1.5.0**

- [x] **Vitest + Testing Library** — 59 testes unitários — v1.3.0 / v1.6.0
  - `format.test.ts` — 21 testes (formatCurrency, formatDate, formatDocumento, labels)
  - `utils.test.ts` — 6 testes (cn / tailwind-merge)
  - `auth.store.test.ts` — 13 testes (login, logout, hasRole, estado inicial)
  - `status-badge.test.tsx` — 10 testes (LocacaoBadge, PagamentoBadge, DisponibilidadeBadge)
  - `auth.service.test.ts` + `catalogo.service.test.ts` — 9 testes (mock axios)
- [x] **Playwright E2E** — 25 testes end-to-end — v1.5.0
  - `auth.spec.ts` — 7 testes (login, guard de rota, validação)
  - `dashboard.spec.ts` — 5 testes (KPIs, navegação)
  - `catalogo.spec.ts` — 5 testes (CRUD dialog, busca, validação)
  - `navegacao.spec.ts` — 8 testes (sidebar, 404, voltar ao painel)

---

## MILESTONE 10 — Configuração para Produção
**Status: CONCLUÍDO — v1.4.0**

- [x] **CORS** — `CorsConfigurationSource` no SecurityConfig; preflight OPTIONS não recebe 401
- [x] **Variáveis de ambiente** — `${VAR:default}` em todo o `application.yml`
- [x] **Profile prod** — `application-prod.yml`: show-sql=false, Swagger desabilitado
- [x] **`.env.example`** — todas as variáveis documentadas com descrição
- [x] **`.gitignore`** — playwright-report/, test-results/ adicionados

---

## PENDENTES — Roadmap v2.0 SaaS Multi-Tenant

### FASE 2 — Multi-Tenancy — CONCLUÍDA v2.0-fase2
- [x] Flyway V7: `tenant_id UUID` em todas as tabelas + índices compostos
- [x] `TenantContext` — ThreadLocal por request, isolamento por thread
- [x] `TenantFilter` — `OncePerRequestFilter`, extrai `tenantId` do JWT (ou header `X-Tenant-Id` para SUPER_ADMIN)
- [x] Tabela `tenants`: id (UUID), nome, slug, plano (FREE/BASIC/PRO), ativo, createdAt, updatedAt
- [x] Módulo `tenants` completo: domain, ports, infra (entity/repo/adapter/mapper), service, controller, DTOs
- [x] `POST /v1/tenants`, `GET /v1/tenants`, `GET /v1/tenants/{id}`, `PUT /v1/tenants/{id}/ativar|desativar` (SUPER_ADMIN)
- [x] Claim `tenantId` no JWT — `JwtService.gerarToken(UserDetails, UUID)` + `extrairTenantId(token)`
- [x] `LoginResponse` com campo `tenantId`
- [x] ROLE_SUPER_ADMIN — acessa todos os tenants; `tenantId = null` no JWT = sem filtro
- [x] Todas as entities JPA com `tenant_id UUID` — queries JPQL filtradas por tenantId
- [x] Todos os services usam `TenantContext.getTenantId()` ao criar/listar entidades
- [x] `DataInitializer` — cria tenant padrão + superadmin/super123 (SUPER_ADMIN sem tenant)
- [x] 9 novos testes: `TenantContextTest` (4) + `TenantServiceTest` (5) — total 156 testes

### FASE 3 — Lógica de Negócio Avançada
- [ ] Política de cancelamento com multa graduada (configurável por tenant)
- [ ] Gestão de caução: `StatusCaucao` enum + fluxo devolução/retenção
- [ ] Registro de danos na devolução (`itemDanificado`, `descricaoDano`, `valorDesconto`)
- [ ] Calendário de ocupação por produto (`GET /v1/catalogo/{id}/agenda`)
- [ ] Locação multi-produto (`itens_locacao` — BREAKING CHANGE)
- [ ] Contrato digital em PDF (`GET /v1/locacoes/{id}/contrato`)

### FASE 4 — Notificações
- [ ] `NotificacaoService` interface + `@Async` + `@EventListener`
- [ ] `EmailNotificacaoAdapter` — Spring Mail + AWS SES
- [ ] `WhatsAppNotificacaoAdapter` — Twilio / Z-API
- [ ] Tabela `notificacoes` — histórico + status de entrega
- [ ] Eventos: reserva criada, lembrete retirada/devolução, atraso, pagamento confirmado

### FASE 5 — Relatórios e Gráficos (obrigatório)
- [ ] Novos endpoints: ocupação, inadimplência, ticket médio, receita por categoria, top clientes
- [ ] Exportação PDF/CSV/XLSX
- [ ] Frontend: Recharts (LineChart, BarChart, PieChart, AreaChart)
- [ ] Dashboard executivo com período selecionável (hoje/semana/mês/trimestre/ano)

### FASE 6 — Upload de Imagens
- [ ] AWS S3 + CloudFront CDN
- [ ] `POST /v1/catalogo/{id}/imagem` — multipart upload + thumbnail
- [ ] Frontend: drag & drop + preview

### FASE 7 — Portal do Cliente
- [ ] Área `/minha-conta` para `ROLE_CLIENTE`
- [ ] Histórico de locações, pagamentos, download de contratos

### FASE 8 — Infraestrutura AWS
- [ ] ECS Fargate + RDS PostgreSQL Multi-AZ
- [ ] AWS S3 + CloudFront + SES + Secrets Manager
- [ ] Application Load Balancer + ACM (HTTPS)
- [ ] GitHub Actions → ECR → ECS (rolling deploy)
- [ ] Frontend: Vercel (gratuito, integração nativa Next.js)

### Débitos técnicos menores
- [ ] Testcontainers — testes de integração com PostgreSQL real
- [ ] Spring Actuator — health-check endpoint (`/actuator/health`)
- [ ] Rate limiting por IP — Bucket4j no endpoint de login
- [ ] Filtro por categoria/tamanho/cor no catálogo (query params)

---

## RESUMO DO PROGRESSO

| Módulo | Backend | Testes Backend | Frontend | Testes Frontend |
|---|---|---|---|---|
| Setup/Config | ✅ | ✅ | ✅ | — |
| Catálogo | ✅ | ✅ 19 | ✅ | ✅ E2E |
| Clientes | ✅ | ✅ | ✅ | ✅ E2E |
| Locações | ✅ | ✅ 44 | ✅ | ✅ E2E |
| Estoque | ✅ | ✅ | ✅ | ✅ E2E |
| Pagamentos | ✅ | ✅ 18 | ✅ | ✅ E2E |
| Segurança JWT | ✅ | ✅ 19 | ✅ | ✅ (unit) |
| Relatórios | ✅ | ✅ 9 | ✅ | ✅ E2E |
| Documentação | ✅ | — | — | — |
| DevOps | ✅ | — | — | — |
| Usuários/Auth | ✅ | ✅ | ✅ | ✅ (unit) |
| Notificações | ❌ | ❌ | — | — |
| Multi-Tenant | ✅ | ✅ 9 | — | — |

**Testes backend:** 156 | **Testes frontend (unit):** 59 | **Testes E2E:** 25
**Releases:** v0.1.0 → v1.6.0 → v2.0-fase1 → v2.0-fase2 | **BUILD SUCCESS**

---

## COMO RODAR LOCALMENTE

### Pré-requisitos
- Java 21 + Maven
- Node.js 20+ + npm
- PostgreSQL rodando na porta 5432 com banco `aluguel`

### 1. Backend
```bash
cd backend/closetRental
./mvnw spring-boot:run
# API disponível em http://localhost:8080
# Swagger em http://localhost:8080/swagger-ui.html
```

### 2. Frontend
```bash
cd frontend
npm install
npm run dev
# App disponível em http://localhost:3000
```

### Contas de teste
| Usuário | Senha | Role |
|---------|-------|------|
| superadmin | super123 | SUPER_ADMIN |
| admin | admin123 | ADMIN |
| atendente | atendente123 | ATENDENTE |
| cliente | cliente123 | CLIENTE |
