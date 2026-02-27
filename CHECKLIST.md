# CHECKLIST COMPLETO — Sistema Closet Rental (Atelier)
> Loja de Aluguel de Roupas & Vestidos | Última atualização: 2026-02-27
> Progresso global: ~35% (Backend) | Frontend: 0%

---

## LEGENDA
- `[ ]` Pendente  |  `[x]` Concluído  |  `[-]` Parcial / Stub  |  `[!]` Atenção / Débito Técnico

---

## MILESTONE 0 — Setup & Infraestrutura Base
**Status: CONCLUÍDO**

- [x] Criar repositório git + estrutura de pastas (`backend/`, `frontend/`)
- [x] `pom.xml` com Java 21, Spring Boot 4.0.0, JPA, Security, Validation, Web MVC
- [x] Dependências: MapStruct 1.6.3, Springdoc OpenAPI 2.7.0, Lombok, PostgreSQL
- [x] `maven-compiler-plugin` com `annotationProcessorPaths` (Lombok + MapStruct juntos)
- [x] `application.yml` com datasource PostgreSQL (`aluguel`, porta 5432)
- [x] `ClosetRentalApplication.java` com `@EnableJpaAuditing`
- [x] `OpenApiConfig.java` — título e versão Swagger em `/swagger-ui.html`
- [x] `SecurityConfig.java` — in-memory (admin, atendente, cliente) + BCrypt
- [x] `GlobalExceptionHandler.java` — handlers para Business, NotFound, Conflict, Validation
- [x] `BusinessException`, `ConflictException`, `EntityNotFoundException`
- [!] **DÉBITO:** `asciidoctor-maven-plugin` no pom.xml sem propósito real — remover ou configurar Spring REST Docs
- [!] **DÉBITO:** Migração Flyway não configurada — `ddl-auto: update` é perigoso em produção
- [ ] Configurar Flyway: `db/migration/V1__create_tables.sql`
- [ ] Configurar profile `dev` vs `prod` no `application.yml`
- [ ] Adicionar `management/actuator` para health-check
- [ ] Configurar CORS para o frontend React/Next.js

---

## MILESTONE 1 — Módulo Catálogo
**Status: IMPLEMENTADO (com 1 stub)**

- [x] `Produto.java` — domain model POJO puro (sem JPA)
- [x] `marcarDisponivel()`, `marcarIndisponivel()`, `calcularValor(dias, caucao)`
- [x] `ProdutoRepositoryPort.java` — interface com todos os métodos necessários
- [x] `ProdutoEntity.java` — @Entity com auditoria (`@CreatedBy`, `@CreatedDate`)
- [x] `ProdutoRepositoryJpa.java` — extends `JpaRepository<ProdutoEntity, Long>`
- [x] `ProdutoRepositoryAdapter.java` — implementa port convertendo Entity↔Domain
- [x] `ProdutoMapper.java` (MapStruct) — `toEntity()` / `toDomain()`
- [x] `ProdutoService.java` — CRUD + validação diária + `existeEmLocacaoAtiva`
- [x] `ProdutoRequest.java` / `ProdutoResponse.java` — DTOs com validação
- [x] `CatalogoController.java` — REST `/v1/catalogo` com paginação
- [-] `buscarDisponiveisNoPeriodo()` — **STUB** (retorna lista vazia); implementar quando módulo locações existir
- [ ] **Adicionar campos faltantes no `Produto`:** `descricao`, `tamanho`, `cor`, `imagemUrl`, `updatedAt`, `updatedBy`
- [ ] **Adicionar `@Index`** no banco: `idx_produtos_disponivel`, `idx_produtos_categoria`
- [ ] **Implementar busca por categoria** com filtros no controller (query params)
- [ ] **Implementar upload de imagem** (S3 ou local) — requisito do documento original
- [ ] **Testes unitários** `ProdutoServiceTest` (cobertura ≥ 80%)
  - [ ] Teste: criar produto válido
  - [ ] Teste: criar produto com valor negativo → exceção
  - [ ] Teste: deletar produto em locação ativa → exceção
  - [ ] Teste: buscar produto inexistente → `EntityNotFoundException`
  - [ ] Teste: atualizar produto
- [ ] **Teste de integração** `CatalogoControllerTest` com `@SpringBootTest`
- [ ] **Teste de segurança:** GET público, POST exige ADMIN/ATENDENTE, DELETE exige ADMIN

---

## MILESTONE 2 — Módulo Clientes
**Status: NÃO INICIADO**

### Domain
- [ ] `Cliente.java` — POJO: `id, nome, email, telefone, documento, endereco, createdAt, createdBy`
- [ ] Método `validarDocumento()` no domain (CPF/CNPJ)
- [ ] `ClienteRepositoryPort.java` — interface: `salvar, buscarPorId, buscarPorDocumento, listarTodos, deletar, existePorEmail, existePorDocumento`

### Infrastructure
- [ ] `ClienteEntity.java` — @Entity tabela `clientes`, `@UniqueConstraint(documento, email)`
- [ ] `ClienteRepositoryJpa.java` — com `findByDocumento()` e `existsByEmail()`
- [ ] `ClienteRepositoryAdapter.java`
- [ ] `ClienteMapper.java` (MapStruct)

### Application
- [ ] `ClienteService.java`
  - [ ] `criar()` — validar email único + documento único (O(1) com índice)
  - [ ] `atualizar()` — sem trocar documento
  - [ ] `buscarPorId()`
  - [ ] `listar()` — paginado obrigatório
  - [ ] `deletar()` — bloquear se tiver locação ativa

### API
- [ ] `ClienteRequest.java` / `ClienteResponse.java`
- [ ] `ClienteController.java` — REST `/v1/clientes`
  - [ ] `POST /v1/clientes` — cadastrar
  - [ ] `GET /v1/clientes` — listar paginado
  - [ ] `GET /v1/clientes/{id}` — buscar
  - [ ] `PUT /v1/clientes/{id}` — atualizar
  - [ ] `DELETE /v1/clientes/{id}` — deletar (regra)
  - [ ] `GET /v1/clientes/{id}/historico` — histórico de locações

### Testes (≥ 80%)
- [ ] `ClienteServiceTest` — documento único, email único, deletar com locação ativa
- [ ] `ClienteControllerTest` — integração + segurança por role
- [ ] Validação regex: email, telefone (formato BR), CPF básico

---

## MILESTONE 3 — Módulo Locações (CORE CRÍTICO)
**Status: NÃO INICIADO**

### Domain
- [ ] `Locacao.java` — POJO: `id, clienteId, produtoId, dataInicio, dataFim, status, valorTotal, caucao, multa, retiradaAt, devolucaoAt, observacoes`
- [ ] `StatusLocacao.java` — enum: `RESERVADO, RETIRADO, DEVOLVIDO, CANCELADO`
- [ ] `LocacaoRepositoryPort.java` — interface: `salvar, buscarPorId, listarPorCliente, listarAtivasNoPeriodo, atualizarStatus, existeOverlapNoPeriodo`

### Regra de Negócio Central (Big-O)
- [ ] **Overlap check** — Query JPQL com índice em `(produto_id, data_inicio, data_fim)` — O(log n)
  ```sql
  WHERE produto_id = :id
    AND status IN ('RESERVADO','RETIRADO')
    AND data_inicio < :dataFim
    AND data_fim > :dataInicio
  ```
- [ ] **Cálculo de valor:** `valorTotal = valorDiaria × dias + caucao`
- [ ] **Cálculo de multa:** `multa = percentualDiario × valorDiaria × diasAtraso`
- [ ] **Cálculo de dias:** `ChronoUnit.DAYS.between(dataInicio, dataFim) + 1`

### Infrastructure
- [ ] `LocacaoEntity.java` — @Entity tabela `locacoes`
  - [ ] `@Index(name = "idx_locacoes_produto_periodo", columnList = "produto_id, data_inicio, data_fim")`
  - [ ] `@Index(name = "idx_locacoes_cliente", columnList = "cliente_id")`
  - [ ] `@Index(name = "idx_locacoes_status", columnList = "status")`
- [ ] `LocacaoRepositoryJpa.java` — com queries nativas/JPQL para overlap
- [ ] `LocacaoRepositoryAdapter.java`
- [ ] `LocacaoMapper.java` (MapStruct)

### Application — Use Cases
- [ ] `CriarReservaService.java`
  - [ ] Validar: `dataInicio < dataFim`
  - [ ] Validar: produto existe e está disponível
  - [ ] Validar: sem overlap de período para o produto
  - [ ] Calcular `valorTotal` e `caucao`
  - [ ] Salvar com status `RESERVADO`
- [ ] `RegistrarRetiradaService.java`
  - [ ] Validar status = `RESERVADO`
  - [ ] Marcar produto como indisponível
  - [ ] Salvar `retiradaAt = now()`
  - [ ] Mudar status → `RETIRADO`
- [ ] `RegistrarDevolucaoService.java`
  - [ ] Validar status = `RETIRADO`
  - [ ] Calcular atraso: `devolucaoAt > dataFim`
  - [ ] Calcular multa se aplicável
  - [ ] Marcar produto como disponível
  - [ ] Registrar movimentação de estoque (ENTRADA)
  - [ ] Salvar `devolucaoAt = now()`
  - [ ] Mudar status → `DEVOLVIDO`
- [ ] `CancelarReservaService.java`
  - [ ] Apenas se status = `RESERVADO`
  - [ ] Mudar status → `CANCELADO`

### API
- [ ] `LocacaoController.java` — REST `/v1/locacoes`
  - [ ] `POST /v1/locacoes/reservar`
  - [ ] `POST /v1/locacoes/{id}/retirar`
  - [ ] `POST /v1/locacoes/{id}/devolver`
  - [ ] `POST /v1/locacoes/{id}/cancelar`
  - [ ] `GET /v1/locacoes/{id}` — detalhes
  - [ ] `GET /v1/locacoes?clienteId=&status=&dataInicio=&dataFim=` — listagem filtrada

### Stub pendente do Catálogo
- [ ] Implementar `buscarDisponiveisNoPeriodo()` em `ProdutoRepositoryJpa`

### Testes (≥ 80%) — CRÍTICO
- [ ] `CriarReservaServiceTest`
  - [ ] Reserva com sucesso — produto disponível, sem overlap
  - [ ] Reserva com overlap → `ConflictException`
  - [ ] Reserva com produto indisponível → `BusinessException`
  - [ ] Reserva com dataInicio >= dataFim → `BusinessException`
  - [ ] Cálculo correto de `valorTotal` e `caucao`
- [ ] `RegistrarRetiradaServiceTest`
  - [ ] Retirada válida de reserva existente
  - [ ] Retirada de locação não-RESERVADA → exceção
- [ ] `RegistrarDevolucaoServiceTest`
  - [ ] Devolução sem atraso — sem multa
  - [ ] Devolução com atraso — cálculo correto de multa
  - [ ] Devolução de locação não-RETIRADA → exceção
- [ ] `CancelarReservaServiceTest`
  - [ ] Cancelamento de RESERVADO → OK
  - [ ] Cancelamento de RETIRADO → `BusinessException`

---

## MILESTONE 4 — Módulo Estoque
**Status: NÃO INICIADO**

- [ ] `MovimentacaoEstoque.java` — domain: `id, produtoId, tipo (ENTRADA, SAIDA, MANUTENCAO), quantidade, motivo, locacaoId, criadoEm`
- [ ] `TipoMovimentacao.java` — enum
- [ ] `EstoqueRepositoryPort.java`
- [ ] `MovimentacaoEstoqueEntity.java` — @Entity tabela `movimentacoes_estoque`
- [ ] `EstoqueService.java`
  - [ ] `registrarSaida()` — chamado por `RegistrarRetiradaService`
  - [ ] `registrarEntrada()` — chamado por `RegistrarDevolucaoService`
  - [ ] `registrarManutencao()` — manual pelo ADMIN
  - [ ] `listarHistorico(produtoId)` — histórico paginado
- [ ] `EstoqueController.java` — REST `/v1/estoque`
  - [ ] `POST /v1/estoque/manutencao` — enviar para manutenção
  - [ ] `GET /v1/estoque/disponibilidade` — visão geral
  - [ ] `GET /v1/estoque/historico/{produtoId}` — histórico de movimentações
- [ ] Testes (≥ 80%)

---

## MILESTONE 5 — Módulo Pagamentos
**Status: NÃO INICIADO**

- [ ] `Pagamento.java` — domain: `id, locacaoId, valor, status, metodo, criadoEm, processadoEm`
- [ ] `StatusPagamento.java` — enum: `PENDENTE, APROVADO, FALHA, ESTORNADO`
- [ ] `MetodoPagamento.java` — enum: `PIX, CARTAO_CREDITO, CARTAO_DEBITO, DINHEIRO`
- [ ] `PagamentoRepositoryPort.java`
- [ ] `PagamentoEntity.java` — @Entity tabela `pagamentos`
- [ ] `GatewayPagamentoPort.java` — interface para mock
- [ ] `GatewayPagamentoMock.java` — simula aprovação/falha
- [ ] `PagamentoService.java`
  - [ ] `processar()` — chama gateway, persiste resultado
  - [ ] `estornar()` — apenas ADMIN
  - [ ] `buscarPorLocacao(locacaoId)`
- [ ] `PagamentoController.java` — REST `/v1/pagamentos`
  - [ ] `POST /v1/pagamentos/pagar`
  - [ ] `POST /v1/pagamentos/{id}/estornar`
  - [ ] `GET /v1/pagamentos/{id}`
  - [ ] `GET /v1/pagamentos?locacaoId=`
- [ ] Testes (≥ 80%)

---

## MILESTONE 6 — Relatórios & Dashboard
**Status: NÃO INICIADO** (Requisito do documento original)

- [ ] `RelatorioController.java` — REST `/v1/relatorios`
  - [ ] `GET /v1/relatorios/disponibilidade?data=` — peças disponíveis em data
  - [ ] `GET /v1/relatorios/historico-cliente/{id}` — histórico de locações do cliente
  - [ ] `GET /v1/relatorios/faturamento?mes=&ano=` — receita no período
  - [ ] `GET /v1/relatorios/pecas-mais-alugadas` — ranking de produtos
  - [ ] `GET /v1/relatorios/atrasos-pendentes` — locações em atraso
- [ ] Queries JPQL otimizadas com agregação (não carregar entidades inteiras)
- [ ] Respostas paginadas para todos os relatórios

---

## MILESTONE 7 — Migração de Banco (Flyway)
**Status: NÃO INICIADO**

- [ ] Adicionar Flyway ao `pom.xml`
- [ ] `V1__create_tables.sql` — tabelas: `produtos, clientes, locacoes, pagamentos, movimentacoes_estoque`
- [ ] `V2__add_indexes.sql` — índices de performance
- [ ] `V3__add_constraints.sql` — constraints de integridade
- [ ] Mudar `ddl-auto` para `validate` em produção

---

## MILESTONE 8 — Segurança (Evolução)
**Status: PARCIAL (in-memory básico)**

- [ ] Migrar para JWT (`spring-security-oauth2-resource-server` ou `jjwt`)
- [ ] `POST /v1/auth/login` — retorna Bearer token
- [ ] `POST /v1/auth/refresh` — refresh token
- [ ] `AuditorAware` personalizado lendo usuário do JWT (hoje usa `SecurityContextHolder`)
- [ ] Rate limiting por IP (ex.: Bucket4j)
- [ ] Conformidade LGPD: campo de consentimento no cliente, mascaramento de CPF nos logs
- [ ] Testes de segurança com `@WithMockUser`

---

## MILESTONE 9 — Testes & Qualidade
**Status: CRÍTICO (0 testes escritos)**

### Tipos de Testes

#### Unitários (Service layer) — JUnit 5 + Mockito
- [ ] `ProdutoServiceTest` — cobertura ≥ 80%
- [ ] `ClienteServiceTest` — cobertura ≥ 80%
- [ ] `CriarReservaServiceTest` — cobertura ≥ 80%
- [ ] `RegistrarRetiradaServiceTest` — cobertura ≥ 80%
- [ ] `RegistrarDevolucaoServiceTest` — cobertura ≥ 80%
- [ ] `CancelarReservaServiceTest` — cobertura ≥ 80%
- [ ] `EstoqueServiceTest` — cobertura ≥ 80%
- [ ] `PagamentoServiceTest` — cobertura ≥ 80%

#### Integração — `@SpringBootTest` + `@Transactional`
- [ ] `CatalogoIntegrationTest` — banco H2 ou Testcontainers PostgreSQL
- [ ] `ClientesIntegrationTest`
- [ ] `LocacoesIntegrationTest` — fluxo completo reserva→retirada→devolução
- [ ] `PagamentosIntegrationTest`

#### Controller (API) — `@WebMvcTest` + MockMvc
- [ ] `CatalogoControllerTest` — todos os endpoints + status HTTP corretos
- [ ] `ClienteControllerTest`
- [ ] `LocacaoControllerTest`
- [ ] `EstoqueControllerTest`
- [ ] `PagamentoControllerTest`

#### Segurança — `@WithMockUser`
- [ ] Testar acesso negado por role em cada endpoint protegido
- [ ] Testar acesso não autenticado → 401

#### Testes de Banco — Testcontainers
- [ ] Configurar `testcontainers-postgresql` no `pom.xml`
- [ ] `AbstractIntegrationTest` com `@Testcontainers`
- [ ] Validar queries de overlap de período
- [ ] Validar índices de performance

#### Relatório de Cobertura
- [ ] Configurar `jacoco-maven-plugin`
- [ ] Meta: ≥ 80% de cobertura em `application/` e `domain/`
- [ ] Gerar relatório HTML: `mvn verify`

---

## MILESTONE 10 — Notificações (Requisito Documento Original)
**Status: NÃO INICIADO** (RF09)

- [ ] `NotificacaoService.java` — interface com métodos `enviarEmail()`, `enviarWhatsApp()`
- [ ] `EmailNotificacaoAdapter.java` — Spring Mail (SMTP ou AWS SES)
- [ ] Notificações automáticas:
  - [ ] Confirmação de reserva
  - [ ] Lembrete 1 dia antes da retirada
  - [ ] Lembrete 1 dia antes da devolução
  - [ ] Aviso de atraso

---

## MILESTONE 11 — Frontend (React / Next.js)
**Status: NÃO INICIADO (pasta vazia)**

### Setup
- [ ] Inicializar projeto Next.js 14+ (App Router) com TypeScript
- [ ] Configurar Tailwind CSS + shadcn/ui
- [ ] Configurar Axios / TanStack Query para chamadas API
- [ ] Configurar Zustand ou Context API para estado global
- [ ] Configurar ESLint + Prettier

### Design System (UI/UX — surpreendente)
> **Tema:** Elegância sombria e sofisticada — Atelier de Moda de Luxo
- [ ] **Paleta:** Deep Rose (`#c2185b`), Champagne (`#f5e6c8`), Onyx (`#1a1a2e`), Ivory (`#faf8f5`), Gold accent (`#d4af37`)
- [ ] **Tipografia:** Playfair Display (headings) + Inter (body) — Google Fonts
- [ ] **Componentes base** (shadcn/ui customizados):
  - [ ] `ProductCard` — imagem, hover com overlay, badge de disponibilidade
  - [ ] `StatusBadge` — cores por status de locação
  - [ ] `DateRangePicker` — seletor de período para reserva
  - [ ] `Timeline` — histórico de estados da locação
  - [ ] `PriceDisplay` — valor formatado em Real com decimais
  - [ ] `AvatarInitials` — avatar com iniciais do cliente
- [ ] **Animações:** Framer Motion — fade-in em cards, slide em modais
- [ ] **Responsivo:** Mobile-first, breakpoints sm/md/lg/xl

### Páginas — Fluxo do Atendente
- [ ] `/login` — página de autenticação com imagem de fundo (moda)
- [ ] `/dashboard` — KPIs em cards: locações hoje, em atraso, receita do mês, peças disponíveis
- [ ] `/catalogo` — grid de produtos com filtros (categoria, disponibilidade, faixa de preço)
- [ ] `/catalogo/novo` — formulário de cadastro de produto com preview de imagem
- [ ] `/catalogo/[id]` — detalhes + histórico de locações da peça
- [ ] `/clientes` — tabela com busca, paginação
- [ ] `/clientes/novo` — formulário de cadastro
- [ ] `/clientes/[id]` — perfil + histórico
- [ ] `/locacoes` — lista de todas as locações com filtros de status + data
- [ ] `/locacoes/nova` — fluxo de reserva em steps (cliente → produto → datas → pagamento → confirmação)
- [ ] `/locacoes/[id]` — detalhes + timeline de estados + botões de ação contextual
- [ ] `/estoque` — visão geral + histórico de movimentações
- [ ] `/relatorios` — gráficos (Recharts): faturamento mensal, peças mais alugadas, heatmap de demanda

### Páginas — Área do Cliente (Portal)
- [ ] `/portal/login` — login simplificado
- [ ] `/portal/minhas-locacoes` — histórico do cliente
- [ ] `/portal/locacoes/[id]` — detalhes (somente leitura)

### Testes Frontend (≥ 80%)
- [ ] Configurar Vitest + Testing Library
- [ ] Testes de componentes: `ProductCard`, `StatusBadge`, `DateRangePicker`
- [ ] Testes de páginas: fluxo de reserva
- [ ] Testes E2E: Playwright
  - [ ] Fluxo completo: login → selecionar produto → reservar → confirmar
  - [ ] Fluxo de devolução
  - [ ] Fluxo de cancelamento

---

## MILESTONE 12 — DevOps & Infraestrutura
**Status: NÃO INICIADO**

- [ ] `docker-compose.yml` — PostgreSQL + backend + frontend (dev)
- [ ] `Dockerfile` backend — multi-stage build
- [ ] `Dockerfile` frontend — Next.js standalone
- [ ] GitHub Actions CI:
  - [ ] Workflow: build + tests + coverage report
  - [ ] Workflow: build Docker e push para registry
- [ ] Variáveis de ambiente seguras (`.env.example`)
- [ ] Monitoramento: Spring Actuator + Prometheus + Grafana
- [ ] Logs centralizados (ELK ou Loki)
- [ ] HTTPS + SSL (Let's Encrypt ou certificado)

---

## DÉBITOS TÉCNICOS & MELHORIAS SUGERIDAS

### Backend (agora)
- [!] **`ddl-auto: update`** — nunca usar em produção. Migrar para Flyway + `validate`
- [!] **asciidoctor plugin** sem Spring REST Docs configurado — limpar pom.xml
- [!] **Autenticação in-memory** — migrar para JWT antes do deploy
- [!] **Produto sem campos ricos** — faltam: `tamanho`, `cor`, `descricao`, `imagemUrl`
- [!] **Sem índices explícitos** — JPQL de overlap sem índice = O(n) → O(log n) com índice composto
- [!] **`@Transactional` na camada de Controller** — mover para Service (já está; manter assim)
- [!] **`show-sql: true` em produção** — desativar, usar apenas em dev
- [ ] Adicionar `@Cacheable` (Spring Cache) para catálogo público (produtos disponíveis)
- [ ] Adicionar paginação obrigatória em TODOS os endpoints de listagem
- [ ] Adicionar `updatedAt` / `updatedBy` em todas as entidades (JPA Auditing)
- [ ] Validar CPF com algoritmo real (não apenas regex)
- [ ] Configurar pool de conexões HikariCP (já vem com Spring Boot, só precisa de ajuste fino)

### DDD (Domain-Driven Design)
- [ ] **Aggregate Root:** `Locacao` deve ser o agregado principal — encapsular mudanças de estado
- [ ] **Value Objects:** criar `Periodo(dataInicio, dataFim)`, `Dinheiro(valor, moeda)`, `Documento(numero, tipo)`
- [ ] **Domain Events:** `ReservaConfirmadaEvent`, `DevolucaoRegistradaEvent` — para desacoplar notificações
- [ ] **Repository no Domain:** ports bem definidas, sem vazar JPA para o domain
- [ ] **Ubiquitous Language:** revisar naming — `retirar` vs `pickup`, `devolver` vs `return`

### Performance (Big-O)
| Operação | Atual | Alvo | Como |
|---|---|---|---|
| Buscar produto por ID | O(1) PK | O(1) | Index PK — OK |
| Listar produtos disponíveis | O(n) scan | O(log n) | Index em `disponivel, categoria` |
| Verificar overlap de locação | O(n) scan | O(log n) | Index composto `(produto_id, data_inicio, data_fim)` |
| Buscar locações por cliente | O(n) scan | O(log n) | Index em `cliente_id` |
| Relatório de faturamento | O(n) | O(1) aggregate | Query com `SUM()` via JPQL |
| Buscar cliente por documento | O(n) scan | O(1) | Unique index em `documento` |

---

## GITHUB ISSUES PRONTAS (colar no GitHub)

### Milestone: `v0.1 — Setup & Catálogo` (FEITO)
```
[DONE] Setup inicial do projeto Spring Boot
[DONE] Módulo Catálogo — CRUD de Produtos
[TODO] Remover asciidoctor-maven-plugin sem configuração
[TODO] Implementar buscarDisponiveisNoPeriodo (stub)
[TODO] Adicionar campos tamanho, cor, descricao, imagemUrl ao Produto
[TODO] Testes unitários ProdutoService (≥80%)
[TODO] Flyway V1__create_tables.sql
```

### Milestone: `v0.2 — Módulo Clientes`
```
[TODO] Domain Cliente + Port
[TODO] ClienteEntity + JPA + Adapter + Mapper
[TODO] ClienteService — CRUD + validações únicas
[TODO] ClienteController REST /v1/clientes
[TODO] Testes unitários ClienteService (≥80%)
[TODO] Teste integração ClienteController
```

### Milestone: `v0.3 — Módulo Locações (CORE)`
```
[TODO] Domain Locacao + StatusLocacao enum
[TODO] LocacaoEntity + índices compostos
[TODO] CriarReservaService — validação overlap O(log n)
[TODO] RegistrarRetiradaService
[TODO] RegistrarDevolucaoService — cálculo multa
[TODO] CancelarReservaService
[TODO] LocacaoController REST /v1/locacoes
[TODO] Testes CriarReservaServiceTest (≥80%)
[TODO] Testes RegistrarDevolucaoServiceTest (≥80%)
[TODO] Implementar stub buscarDisponiveisNoPeriodo
```

### Milestone: `v0.4 — Estoque & Pagamentos`
```
[TODO] Domain MovimentacaoEstoque
[TODO] EstoqueService — integração com Locações
[TODO] EstoqueController /v1/estoque
[TODO] Domain Pagamento + GatewayMock
[TODO] PagamentoService
[TODO] PagamentoController /v1/pagamentos
[TODO] Testes ≥80% ambos módulos
```

### Milestone: `v0.5 — Segurança, Flyway & Qualidade`
```
[TODO] Migrar in-memory → JWT
[TODO] AuditorAware lendo JWT
[TODO] Flyway V1, V2 (índices), V3 (constraints)
[TODO] Jacoco — relatório de cobertura
[TODO] Testcontainers para testes de integração
[TODO] Rate limiting
[TODO] Conformidade LGPD básica
```

### Milestone: `v1.0 — Frontend`
```
[TODO] Setup Next.js + Tailwind + shadcn/ui
[TODO] Design System — paleta, tipografia, componentes
[TODO] Tela de Login
[TODO] Dashboard com KPIs
[TODO] Catálogo — grid com filtros
[TODO] Fluxo de Reserva em steps
[TODO] Tela de Locações — timeline de estados
[TODO] Relatórios com gráficos (Recharts)
[TODO] Portal do Cliente
[TODO] Testes Vitest + Playwright E2E
```

### Milestone: `v1.1 — Notificações & DevOps`
```
[TODO] NotificacaoService — Email (Spring Mail)
[TODO] Notificações automáticas de reserva/devolução
[TODO] Docker Compose (dev)
[TODO] Dockerfile backend e frontend
[TODO] GitHub Actions CI/CD
[TODO] Monitoramento: Actuator + Prometheus + Grafana
```

---

## VISÃO DE UI/UX — CONCEITO VISUAL

```
┌────────────────────────────────────────────────────────────────┐
│  🎭 ATELIER CLOSET                          ☰ Menu   👤 Admin  │
│  ─────────────────────────────────────────────────────────────  │
│                                                                │
│  DASHBOARD                                                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐      │
│  │ 📅 HOJE  │  │ ⚠️ ATRASO│  │💰 RECEITA│  │👗 DISP.  │      │
│  │    12    │  │    3     │  │ R$4.200  │  │   47/60  │      │
│  │locações  │  │ clientes │  │  mês    │  │  peças   │      │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘      │
│                                                                │
│  CATÁLOGO ─────────────────────────── Filtros ▼ + Novo       │
│  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐             │
│  │  [img] │  │  [img] │  │  [img] │  │  [img] │             │
│  │Vestido │  │ Terno  │  │Noiva   │  │Acessório│            │
│  │Longo   │  │ Slim   │  │Premium │  │Tiara    │            │
│  │● Disp  │  │● Disp  │  │◌ Alug. │  │● Disp  │            │
│  │R$89/d  │  │R$65/d  │  │R$350/d │  │R$25/d  │            │
│  └────────┘  └────────┘  └────────┘  └────────┘             │
│                                                                │
│  LOCAÇÃO #1052 ─────────────────────────────────────────────  │
│  Cliente: Maria Silva   Produto: Vestido Longo Rose           │
│                                                                │
│  ●───────●───────●───────○                                    │
│  RESERVADO  RETIRADO  DEVOLVIDO  CANCELADO                    │
│  10/dez    13/dez    15/dez                                   │
│                                                                │
│  Valor: R$267,00  Caução: R$100,00  Status: ✅ DEVOLVIDO     │
└────────────────────────────────────────────────────────────────┘

PALETA DE CORES:
  Fundo principal:  #1a1a2e (Onyx profundo)
  Superfície cards: #16213e (Azul noite)
  Accent primário:  #c2185b (Rose escuro — elegância)
  Accent dourado:   #d4af37 (Gold — luxo)
  Texto claro:      #faf8f5 (Ivory)
  Texto sutil:      #a0a0b0 (Cinza)
  Disponível:       #4caf50 (Verde)
  Alugado:          #ff9800 (Âmbar)
  Atraso:           #f44336 (Vermelho)
  Cancelado:        #607d8b (Cinza azulado)

TIPOGRAFIA:
  Headings: Playfair Display (serif — moda/elegância)
  Body:     Inter (sans-serif — leitura)
  Mono:     JetBrains Mono (código/valores)

DIFERENCIAL VISUAL:
  → Cards de produto com efeito glassmorphism no hover
  → Timeline de locação animada com Framer Motion
  → Dashboard com gradiente sutil Onyx → Deep Blue
  → Sidebar colapsável com ícones ilustrativos
  → Notificações in-app com slide-in toast
  → Calendário de disponibilidade visual (heatmap)
  → Upload de foto de produto com preview instantâneo
  → Print/PDF de contrato de locação diretamente na tela
```

---

## RESUMO DO PROGRESSO

| Módulo | Backend | Testes | Frontend |
|---|---|---|---|
| Setup/Config | ✅ 100% | - | ❌ 0% |
| Catálogo | ✅ 95% (1 stub) | ❌ 0% | ❌ 0% |
| Clientes | ❌ 0% | ❌ 0% | ❌ 0% |
| Locações | ❌ 0% | ❌ 0% | ❌ 0% |
| Estoque | ❌ 0% | ❌ 0% | ❌ 0% |
| Pagamentos | ❌ 0% | ❌ 0% | ❌ 0% |
| Relatórios | ❌ 0% | ❌ 0% | ❌ 0% |
| Notificações | ❌ 0% | ❌ 0% | ❌ 0% |
| DevOps | ❌ 0% | - | - |

**Próxima etapa recomendada:** Módulo Clientes → Módulo Locações (Core)
