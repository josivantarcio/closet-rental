terraform {
  required_version = ">= 1.7"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Backend remoto — armazena state no S3 com lock via DynamoDB.
  # Criar o bucket e a tabela manualmente antes do primeiro `terraform init`.
  # Veja: infra/terraform/bootstrap/README.md
  backend "s3" {
    bucket         = "closet-rental-tfstate"
    key            = "prod/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "closet-rental-tflock"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "closet-rental"
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}

# ==============================================================================
# Módulos
# ==============================================================================

module "vpc" {
  source      = "./modules/vpc"
  project     = var.project
  environment = var.environment
  az_count    = 2
}

module "ecr" {
  source      = "./modules/ecr"
  project     = var.project
  environment = var.environment
}

module "rds" {
  source             = "./modules/rds"
  project            = var.project
  environment        = var.environment
  vpc_id             = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
  db_name            = var.db_name
  db_username        = var.db_username
  db_password        = var.db_password
  instance_class     = var.rds_instance_class
}

module "secrets" {
  source        = "./modules/secrets"
  project       = var.project
  environment   = var.environment
  jwt_secret    = var.jwt_secret
  db_url        = "jdbc:postgresql://${module.rds.endpoint}/${var.db_name}"
  db_username   = var.db_username
  db_password   = var.db_password
  mail_host     = var.mail_host
  mail_username = var.mail_username
  mail_password = var.mail_password
  cors_origins  = var.cors_origins
}

module "s3" {
  source      = "./modules/s3"
  project     = var.project
  environment = var.environment
}

module "alb" {
  source            = "./modules/alb"
  project           = var.project
  environment       = var.environment
  vpc_id            = module.vpc.vpc_id
  public_subnet_ids = module.vpc.public_subnet_ids
  certificate_arn   = var.acm_certificate_arn
}

module "ecs" {
  source               = "./modules/ecs"
  project              = var.project
  environment          = var.environment
  vpc_id               = module.vpc.vpc_id
  private_subnet_ids   = module.vpc.private_subnet_ids
  ecr_repository_url   = module.ecr.repository_url
  image_tag            = var.image_tag
  alb_target_group_arn = module.alb.target_group_arn
  alb_security_group_id = module.alb.security_group_id
  rds_security_group_id = module.rds.security_group_id
  secrets_arn          = module.secrets.secret_arn
  task_cpu             = var.ecs_task_cpu
  task_memory          = var.ecs_task_memory
  desired_count        = var.ecs_desired_count
}
