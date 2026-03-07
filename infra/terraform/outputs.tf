output "alb_dns_name" {
  description = "DNS do Application Load Balancer — configure como CNAME no seu domínio"
  value       = module.alb.dns_name
}

output "ecr_repository_url" {
  description = "URL do repositório ECR para push de imagens"
  value       = module.ecr.repository_url
}

output "rds_endpoint" {
  description = "Endpoint do RDS PostgreSQL (host:port)"
  value       = module.rds.endpoint
  sensitive   = true
}

output "s3_bucket_name" {
  description = "Nome do bucket S3 para imagens"
  value       = module.s3.bucket_name
}

output "cloudfront_url" {
  description = "URL do CloudFront (CDN das imagens)"
  value       = module.s3.cloudfront_url
}

output "ecs_cluster_name" {
  description = "Nome do cluster ECS"
  value       = module.ecs.cluster_name
}

output "ecs_service_name" {
  description = "Nome do serviço ECS (usado no deploy)"
  value       = module.ecs.service_name
}
