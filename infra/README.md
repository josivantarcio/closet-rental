# Infraestrutura AWS — Closet Rental

Terraform IaC para o ambiente de produção do Closet Rental SaaS.

## Arquitetura

```
Internet → Route 53 (opcional) → ALB (HTTPS/TLS 1.3) → ECS Fargate (Spring Boot)
                                                              ↓
                                                    RDS PostgreSQL 16
                                                    S3 + CloudFront (imagens)
                                                    SES (e-mail)
                                                    Secrets Manager (segredos)
```

## Pré-requisitos

- AWS CLI configurado: `aws configure`
- Terraform ≥ 1.7
- Conta AWS com permissões para criar VPC, ECS, RDS, S3, CloudFront, SES, IAM
- Domínio registrado (para ACM + SES) — opcional, mas recomendado

## Primeiro deploy (bootstrap + apply)

### 1. Criar o backend do Terraform (S3 + DynamoDB)

```bash
cd infra/terraform/bootstrap
chmod +x bootstrap.sh
./bootstrap.sh
```

### 2. Configurar variáveis

```bash
cd infra/terraform
cp terraform.tfvars.example terraform.tfvars
# Edite terraform.tfvars com seus valores reais
```

Variáveis obrigatórias em `terraform.tfvars`:

| Variável            | Descrição                                    |
|---------------------|----------------------------------------------|
| `aws_region`        | Região AWS (ex: `us-east-1`)                 |
| `project_name`      | Nome do projeto (ex: `closet-rental`)        |
| `environment`       | Ambiente (ex: `prod`)                        |
| `db_password`       | Senha do banco RDS (mínimo 16 chars)         |
| `jwt_secret`        | Segredo JWT HS256 (`openssl rand -hex 32`)   |
| `mail_username`     | Usuário SMTP SES (output do Terraform)       |
| `mail_password`     | Senha SMTP SES (output do Terraform)         |
| `cors_allowed_origins` | URL do frontend Netlify                   |
| `certificate_arn`   | ARN do certificado ACM para o domínio        |
| `ses_domain`        | Domínio verificado no SES                    |

### 3. Inicializar e aplicar

```bash
terraform init
terraform plan -out=tfplan
terraform apply tfplan
```

### 4. Verificar outputs

```bash
terraform output
```

Outputs importantes:

- `alb_dns_name` — DNS do ALB (configurar CNAME no Route 53 ou provedor DNS)
- `ecr_repository_url` — URL do repositório ECR para o GitHub Actions
- `cloudfront_url` — URL pública para servir imagens
- `rds_endpoint` — Endpoint do banco (vai para o Secrets Manager)

### 5. Verificar SES DKIM

Adicione os registros DKIM ao seu DNS:

```bash
terraform output -json dkim_tokens
```

Adicione 3 registros CNAME `<token>._domainkey.<seu-dominio>` apontando para `<token>.dkim.amazonses.com`.

## GitHub Actions — Secrets e Variables necessários

Em **Settings → Secrets → Actions**:

| Secret                   | Valor                              |
|--------------------------|------------------------------------|
| `AWS_ACCESS_KEY_ID`      | Chave IAM com permissões ECR + ECS |
| `AWS_SECRET_ACCESS_KEY`  | Chave secreta IAM                  |
| `AWS_ACCOUNT_ID`         | ID da conta AWS (12 dígitos)       |

Em **Settings → Variables → Actions**:

| Variable          | Valor                         |
|-------------------|-------------------------------|
| `AWS_REGION`      | `us-east-1`                   |
| `ECR_REPOSITORY`  | `closet-rental-prod`          |
| `ECS_CLUSTER`     | `closet-rental-prod`          |
| `ECS_SERVICE`     | `closet-rental-prod-svc`      |

## Deploy subsequente

O pipeline `.github/workflows/deploy.yml` é acionado automaticamente em cada `push` para `main` que altere arquivos em `backend/`.

Fluxo: `test` → `build-and-push` (ECR) → `deploy` (ECS rolling update)

## Rollback manual

```bash
# Listar revisões anteriores da task definition
aws ecs list-task-definitions --family-prefix closet-rental-prod --sort DESC

# Forçar nova task com revisão anterior
aws ecs update-service \
  --cluster closet-rental-prod \
  --service closet-rental-prod-svc \
  --task-definition closet-rental-prod:<REVISAO_ANTERIOR>
```

## Custos estimados (us-east-1, mês)

| Recurso               | Tipo                  | Custo aprox. |
|-----------------------|-----------------------|--------------|
| ECS Fargate           | 0.25 vCPU / 0.5 GB   | ~$7          |
| RDS PostgreSQL        | db.t4g.micro          | ~$15         |
| ALB                   | 1 instância           | ~$17         |
| NAT Gateway           | 1 AZ                  | ~$35         |
| S3 + CloudFront       | < 10 GB               | ~$2          |
| Secrets Manager       | 1 secret              | ~$0.40       |
| **Total estimado**    |                       | **~$77/mês** |

> Para reduzir custo: remova o NAT Gateway usando VPC Endpoints para ECR/S3/Secrets Manager (adiciona ~$22/mês em endpoints, mas remove ~$35 do NAT).

## Destruir ambiente

```bash
# CUIDADO: destrói TODOS os recursos incluindo o banco
terraform destroy
```
