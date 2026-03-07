variable "aws_region" {
  description = "Região AWS"
  type        = string
  default     = "us-east-1"
}

variable "project" {
  description = "Nome do projeto (usado em tags e nomes de recursos)"
  type        = string
  default     = "closet-rental"
}

variable "environment" {
  description = "Ambiente (prod | staging)"
  type        = string
  default     = "prod"
}

# --- RDS ---
variable "db_name" {
  description = "Nome do banco PostgreSQL"
  type        = string
  default     = "aluguel"
}

variable "db_username" {
  description = "Usuário master do RDS"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Senha master do RDS"
  type        = string
  sensitive   = true
}

variable "rds_instance_class" {
  description = "Classe da instância RDS"
  type        = string
  default     = "db.t3.micro"
}

# --- ECS ---
variable "image_tag" {
  description = "Tag da imagem Docker (ex: sha-abc1234)"
  type        = string
  default     = "latest"
}

variable "ecs_task_cpu" {
  description = "CPU para a task ECS (unidades: 256=0.25vCPU)"
  type        = number
  default     = 512
}

variable "ecs_task_memory" {
  description = "Memória para a task ECS (MB)"
  type        = number
  default     = 1024
}

variable "ecs_desired_count" {
  description = "Número de tasks ECS rodando"
  type        = number
  default     = 1
}

# --- ALB / ACM ---
variable "acm_certificate_arn" {
  description = "ARN do certificado ACM para HTTPS (us-east-1)"
  type        = string
}

# --- Segredos da aplicação ---
variable "jwt_secret" {
  description = "Chave HS256 para JWT (mínimo 64 hex chars)"
  type        = string
  sensitive   = true
}

variable "mail_host" {
  description = "SMTP host (ex: email-smtp.us-east-1.amazonaws.com para SES)"
  type        = string
  default     = "email-smtp.us-east-1.amazonaws.com"
}

variable "mail_username" {
  description = "Usuário SMTP (credencial SMTP do SES)"
  type        = string
  sensitive   = true
}

variable "mail_password" {
  description = "Senha SMTP (credencial SMTP do SES)"
  type        = string
  sensitive   = true
}

variable "cors_origins" {
  description = "Origens permitidas no CORS (URL do Netlify)"
  type        = string
}
