# Closet Rental — Sistema de Aluguel de Peças de Roupa

API REST para gerenciamento completo de um atelier de aluguel de roupas: catálogo, clientes,
locações, estoque e pagamentos. Arquitetura Clean Architecture modular, JWT stateless e
documentação OpenAPI 3 integrada.

---

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.0.0 |
| Persistência | Spring Data JPA + PostgreSQL 16 |
| Segurança | Spring Security + JWT (jjwt 0.12.6) |
| Mapeamento | MapStruct 1.6.3 |
| Documentação | Springdoc OpenAPI 2.7.0 (Swagger UI) |
| Testes | JUnit 5 + Mockito + H2 (in-memory) |
| Build | Maven 3.9+ |

---

## Pré-requisitos

- Java 21+, Maven 3.9+ *(apenas para backend local)*
- Node.js 20+ *(apenas para frontend local)*
- Docker + Docker Compose *(opção recomendada: sobe tudo automaticamente)*

---

## Execução com Docker (recomendado)

```bash
# Sobe PostgreSQL + backend Spring Boot (primeira vez faz o build da imagem)
docker compose up -d

# Frontend (em outro terminal)
cd frontend
npm install
npm run dev
```

- API: `http://localhost:8080`
- Frontend: `http://localhost:3001`
- Swagger: `http://localhost:8080/swagger-ui.html`

> O banco PostgreSQL e o schema são criados automaticamente (Flyway).
> O build da imagem Docker ocorre apenas na primeira vez ou quando houver mudanças no backend.

---

## Execução local (sem Docker)

### Backend
```bash
# Certifique-se de ter PostgreSQL rodando em localhost:5432 com banco 'aluguel'
cd backend/closetRental
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev   # http://localhost:3001
```

---

## Contas de acesso

| Usuário | Senha | Perfil |
|---|---|---|
| `admin` | `admin123` | Administrador completo |
| `atendente` | `atendente123` | Operações do atelier |
| `cliente` | `cliente123` | Visualização |

---

---

## Documentação interativa (Swagger UI)

```
http://localhost:8080/swagger-ui.html
```

Para testar endpoints protegidos no Swagger:
1. Faça login em **POST /v1/auth/login** e copie o `token` da resposta.
2. Clique em **Authorize** (cadeado) e informe: `Bearer <token>`.

---

## Autenticação

Todos os endpoints — exceto `GET /v1/catalogo` e `POST /v1/auth/login` — exigem um Bearer JWT.

### Login

```bash
curl -s -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq .
```

Resposta:

```json
{
  "token": "eyJhbGci...",
  "tipo": "Bearer",
  "expiresIn": 86400,
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}
```

### Usuários disponíveis

| Username | Senha | Role |
|---|---|---|
| `admin` | `admin123` | `ROLE_ADMIN` |
| `atendente` | `atendente123` | `ROLE_ATENDENTE` |
| `cliente` | `cliente123` | `ROLE_CLIENTE` |

### Usar o token

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r .token)

curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/v1/clientes
```

---

## Endpoints

### Autenticação

| Método | Path | Auth | Descrição |
|---|---|---|---|
| POST | `/v1/auth/login` | Público | Gerar token JWT |

### Catálogo

| Método | Path | Role mínima | Descrição |
|---|---|---|---|
| GET | `/v1/catalogo` | Público | Listar produtos (paginado) |
| GET | `/v1/catalogo/{id}` | Público | Buscar produto por ID |
| POST | `/v1/catalogo` | ATENDENTE | Cadastrar produto |
| PUT | `/v1/catalogo/{id}` | ATENDENTE | Atualizar produto |
| DELETE | `/v1/catalogo/{id}` | ADMIN | Excluir produto |

### Clientes

| Método | Path | Role mínima | Descrição |
|---|---|---|---|
| GET | `/v1/clientes` | ATENDENTE | Listar clientes (paginado) |
| GET | `/v1/clientes/{id}` | ATENDENTE | Buscar cliente por ID |
| POST | `/v1/clientes` | ATENDENTE | Cadastrar cliente |
| PUT | `/v1/clientes/{id}` | ATENDENTE | Atualizar cliente |
| DELETE | `/v1/clientes/{id}` | ADMIN | Excluir cliente |
| PATCH | `/v1/clientes/{id}/inativar` | ATENDENTE | Inativar (preserva histórico) |
| PATCH | `/v1/clientes/{id}/reativar` | ATENDENTE | Reativar cliente inativo |

### Locações

| Método | Path | Role mínima | Descrição |
|---|---|---|---|
| POST | `/v1/locacoes/reservar` | ATENDENTE | Criar reserva |
| POST | `/v1/locacoes/{id}/retirar` | ATENDENTE | Registrar retirada |
| POST | `/v1/locacoes/{id}/devolver` | ATENDENTE | Registrar devolução (calcula multa) |
| POST | `/v1/locacoes/{id}/cancelar` | ATENDENTE | Cancelar reserva |
| GET | `/v1/locacoes/{id}` | ATENDENTE | Buscar locação por ID |
| GET | `/v1/locacoes` | ATENDENTE | Listar (filtros: `clienteId`, `status`) |

### Estoque

| Método | Path | Role mínima | Descrição |
|---|---|---|---|
| GET | `/v1/estoque/disponibilidade/{produtoId}` | ATENDENTE | Verificar disponibilidade |
| POST | `/v1/estoque/manutencao` | ATENDENTE | Enviar peça para manutenção |
| POST | `/v1/estoque/manutencao/retorno` | ATENDENTE | Registrar retorno da manutenção |
| GET | `/v1/estoque/historico/{produtoId}` | ATENDENTE | Histórico de uma peça |
| GET | `/v1/estoque/historico` | ATENDENTE | Histórico completo (filtro: `tipo`) |

### Pagamentos

| Método | Path | Role mínima | Descrição |
|---|---|---|---|
| POST | `/v1/pagamentos` | ATENDENTE | Processar pagamento |
| POST | `/v1/pagamentos/{id}/estornar` | ADMIN | Estornar pagamento aprovado |
| GET | `/v1/pagamentos/{id}` | ATENDENTE | Buscar pagamento por ID |
| GET | `/v1/pagamentos` | ATENDENTE | Listar (filtros: `locacaoId`, `status`) |

---

## Fluxo completo — exemplo com curl

```bash
# 1. Autenticar
TOKEN=$(curl -s -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r .token)

AUTH="Authorization: Bearer $TOKEN"

# 2. Cadastrar produto no catálogo
curl -s -X POST http://localhost:8080/v1/catalogo \
  -H "Content-Type: application/json" -H "$AUTH" \
  -d '{"nome":"Vestido Longo Preto","categoria":"VESTIDO","valorDiaria":85.00,"disponivel":true}'

# 3. Cadastrar cliente
curl -s -X POST http://localhost:8080/v1/clientes \
  -H "Content-Type: application/json" -H "$AUTH" \
  -d '{"nome":"Maria Silva","email":"maria@email.com","documento":"123.456.789-00","telefone":"11999990000"}'

# 4. Criar reserva (produtoId=1, clienteId=1)
curl -s -X POST http://localhost:8080/v1/locacoes/reservar \
  -H "Content-Type: application/json" -H "$AUTH" \
  -d '{"clienteId":1,"produtoId":1,"dataInicio":"2026-03-01","dataFim":"2026-03-03","caucao":100.00}'

# 5. Registrar retirada (locacaoId=1)
curl -s -X POST http://localhost:8080/v1/locacoes/1/retirar -H "$AUTH"

# 6. Registrar devolução
curl -s -X POST http://localhost:8080/v1/locacoes/1/devolver -H "$AUTH"

# 7. Processar pagamento via PIX
curl -s -X POST http://localhost:8080/v1/pagamentos \
  -H "Content-Type: application/json" -H "$AUTH" \
  -d '{"locacaoId":1,"valor":270.00,"metodo":"PIX"}'
```

---

## Fluxo de estados das Locações

```
RESERVADO → RETIRADO → DEVOLVIDO
RESERVADO → CANCELADO
```

Transições inválidas retornam `400 Bad Request`.

---

## Fluxo de estados dos Pagamentos

```
PENDENTE → APROVADO
PENDENTE → FALHA
APROVADO → ESTORNADO
```

---

## Gateway de Pagamento (mock)

O gateway é simulado sem chamadas externas:

| Método | Comportamento |
|---|---|
| PIX | Sempre aprovado |
| DINHEIRO | Sempre aprovado |
| CARTÃO CRÉDITO | 10% de chance de falha |
| CARTÃO DÉBITO | 10% de chance de falha |

A taxa de falha é configurável via `app.pagamento.gateway.taxa-falha`.

---

## Executar testes

```bash
cd backend/closetRental

# Todos os testes (119 testes, ~30s)
mvn test

# Apenas testes de segurança
mvn test -Dtest="AuthControllerTest,SecurityRolesTest"

# Apenas testes do módulo pagamentos
mvn test -Dtest="ProcessarPagamentoServiceTest,EstornarPagamentoServiceTest,PagamentoQueryServiceTest"
```

Os testes usam banco H2 em memória (profile `test`) — PostgreSQL não é necessário para rodar os testes.

---

## Arquitetura

```
com.jtarcio.closetrental/
├── shared/
│   ├── config/          → SecurityConfig, OpenApiConfig
│   ├── exception/       → BusinessException, ConflictException, GlobalExceptionHandler
│   └── security/        → JwtService, JwtAuthenticationFilter, JwtAuditorAware
└── modules/
    ├── auth/            → AuthController, LoginRequest/Response
    ├── catalogo/        → Produto: CRUD + paginação
    ├── clientes/        → Cliente: CRUD + inativar/reativar
    ├── locacoes/        → 4 use cases: reservar/retirar/devolver/cancelar
    ├── estoque/         → movimentações + histórico
    └── pagamentos/      → processar/estornar + gateway mock
```

Cada módulo segue a estrutura:

```
<modulo>/
  api/           → Controller + DTOs (Request/Response)
  application/   → Services / Use Cases
  domain/        → Models (sem JPA) + Ports (interfaces)
  infrastructure → Entities JPA + Repositories + Adapters + Mappers
```

---

## Variáveis de configuração

| Propriedade | Padrão | Descrição |
|---|---|---|
| `server.port` | `8080` | Porta do servidor |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/aluguel` | URL do banco |
| `app.jwt.secret` | — (obrigatória, via `JWT_SECRET`) | Chave HS256 do JWT |
| `app.jwt.expiration` | `86400` | Expiração do token em segundos (24h) |
| `app.locacao.caucao-padrao` | `100.00` | Caução padrão |
| `app.locacao.percentual-multa-diaria` | `0.10` | Multa por dia de atraso (10%) |
| `app.pagamento.gateway.taxa-falha` | `0.10` | Taxa de falha do gateway mock (10%) |

---

## Frontend — Funcionalidades

| Página | Funcionalidades |
|---|---|
| `/login` | Autenticação JWT + hint de contas de teste |
| `/dashboard` | KPIs: peças em uso, atrasos, receita, catálogo total |
| `/catalogo` | CRUD produtos + toggle grid/lista + busca global |
| `/clientes` | CRUD clientes + inativar/reativar + CPF mascarado (LGPD) |
| `/locacoes` | Lista filtrada por status + Nova Reserva (Combobox) + nomes reais |
| `/locacoes/[id]` | Detalhe + timeline + processar pagamento + histórico de pagamentos |
| `/estoque` | Cards resumo + histórico + manutenção/retorno via Combobox |
| `/pagamentos` | Cards resumo (Aprovados/Receita/Estornados/Falhas) + estorno + filtro |
| `/relatorios` | Faturamento mensal + top 10 peças + atrasos pendentes |

---

## Débitos técnicos conhecidos

- Autenticação in-memory — substituir por banco de usuários (PostgreSQL)
- Produto sem campos: `tamanho`, `cor`, `descricao`, `imagemUrl`
- Cobertura de testes: módulos api/infrastructure sem testes de integração
- `ProdutoServiceTest` — cobertura unitária completa pendente
