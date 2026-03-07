#!/usr/bin/env bash
# =============================================================================
# bootstrap.sh — Cria o bucket S3 e tabela DynamoDB para o Terraform state
# Execute UMA ÚNICA VEZ antes do primeiro `terraform init`.
#
# Pré-requisitos:
#   aws cli configurado: aws configure
# =============================================================================

set -euo pipefail

REGION="us-east-1"
BUCKET="closet-rental-tfstate"
TABLE="closet-rental-tflock"

echo "▶ Criando bucket S3 para Terraform state..."
aws s3api create-bucket \
  --bucket "$BUCKET" \
  --region "$REGION" \
  --create-bucket-configuration LocationConstraint="$REGION" 2>/dev/null || \
  echo "  (bucket já existe, continuando)"

aws s3api put-bucket-versioning \
  --bucket "$BUCKET" \
  --versioning-configuration Status=Enabled

aws s3api put-bucket-encryption \
  --bucket "$BUCKET" \
  --server-side-encryption-configuration \
    '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'

aws s3api put-public-access-block \
  --bucket "$BUCKET" \
  --public-access-block-configuration \
    "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"

echo "▶ Criando tabela DynamoDB para lock..."
aws dynamodb create-table \
  --table-name "$TABLE" \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region "$REGION" 2>/dev/null || \
  echo "  (tabela já existe, continuando)"

echo ""
echo "✅ Bootstrap concluído!"
echo "   Bucket: s3://$BUCKET"
echo "   DynamoDB: $TABLE (região: $REGION)"
echo ""
echo "Próximo passo:"
echo "  cd infra/terraform"
echo "  cp terraform.tfvars.example terraform.tfvars"
echo "  # edite terraform.tfvars com seus valores"
echo "  terraform init"
echo "  terraform plan"
echo "  terraform apply"
