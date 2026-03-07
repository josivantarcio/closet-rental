data "aws_region" "current" {}
data "aws_caller_identity" "current" {}

# IAM Role para execução das tasks (pull imagem do ECR, ler Secrets Manager, logs)
resource "aws_iam_role" "ecs_execution" {
  name = "${var.project}-${var.environment}-ecs-exec-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_execution" {
  role       = aws_iam_role.ecs_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Permissão para ler o Secrets Manager
resource "aws_iam_role_policy" "secrets_access" {
  name = "secrets-access"
  role = aws_iam_role.ecs_execution.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["secretsmanager:GetSecretValue"]
      Resource = [var.secrets_arn]
    }]
  })
}

# IAM Role para a task em si (acesso ao S3 para upload de imagens)
resource "aws_iam_role" "ecs_task" {
  name = "${var.project}-${var.environment}-ecs-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy" "s3_access" {
  name = "s3-images-access"
  role = aws_iam_role.ecs_task.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ]
      Resource = [
        "arn:aws:s3:::${var.project}-${var.environment}-images",
        "arn:aws:s3:::${var.project}-${var.environment}-images/*"
      ]
    }]
  })
}

# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/${var.project}-${var.environment}"
  retention_in_days = 30
  tags              = { Name = "${var.project}-${var.environment}-logs" }
}

# Security Group da task ECS — recebe tráfego apenas do ALB
resource "aws_security_group" "ecs_tasks" {
  name        = "${var.project}-${var.environment}-ecs-sg"
  description = "ECS tasks — acesso apenas do ALB"
  vpc_id      = var.vpc_id

  ingress {
    description     = "HTTP from ALB"
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [var.alb_security_group_id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project}-${var.environment}-ecs-sg" }
}

# Permite ECS acessar RDS
resource "aws_security_group_rule" "ecs_to_rds" {
  type                     = "ingress"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  security_group_id        = var.rds_security_group_id
  source_security_group_id = aws_security_group.ecs_tasks.id
  description              = "ECS tasks to RDS"
}

# ECS Cluster
resource "aws_ecs_cluster" "main" {
  name = "${var.project}-${var.environment}"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = { Name = "${var.project}-${var.environment}-cluster" }
}

resource "aws_ecs_cluster_capacity_providers" "main" {
  cluster_name       = aws_ecs_cluster.main.name
  capacity_providers = ["FARGATE", "FARGATE_SPOT"]

  default_capacity_provider_strategy {
    capacity_provider = "FARGATE"
    weight            = 1
  }
}

# Task Definition
resource "aws_ecs_task_definition" "app" {
  family                   = "${var.project}-${var.environment}"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([{
    name      = "api"
    image     = "${var.ecr_repository_url}:${var.image_tag}"
    essential = true

    portMappings = [{
      containerPort = 8080
      protocol      = "tcp"
    }]

    # Variáveis não-sensíveis
    environment = [
      { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
      { name = "APP_PORT",               value = "8080" },
      { name = "STORAGE_PROVIDER",       value = "s3" },
      { name = "AWS_REGION",             value = data.aws_region.current.name },
      { name = "S3_BUCKET",              value = "${var.project}-${var.environment}-images" },
      { name = "MAIL_ENABLED",           value = "true" },
      { name = "SHOW_SQL",               value = "false" }
    ]

    # Segredos injetados do Secrets Manager (individualmente via jsonKey)
    secrets = [
      { name = "SPRING_DATASOURCE_URL",      valueFrom = "${var.secrets_arn}:SPRING_DATASOURCE_URL::" },
      { name = "SPRING_DATASOURCE_USERNAME", valueFrom = "${var.secrets_arn}:SPRING_DATASOURCE_USERNAME::" },
      { name = "SPRING_DATASOURCE_PASSWORD", valueFrom = "${var.secrets_arn}:SPRING_DATASOURCE_PASSWORD::" },
      { name = "APP_JWT_SECRET",             valueFrom = "${var.secrets_arn}:APP_JWT_SECRET::" },
      { name = "MAIL_HOST",                  valueFrom = "${var.secrets_arn}:MAIL_HOST::" },
      { name = "MAIL_USERNAME",              valueFrom = "${var.secrets_arn}:MAIL_USERNAME::" },
      { name = "MAIL_PASSWORD",              valueFrom = "${var.secrets_arn}:MAIL_PASSWORD::" },
      { name = "CORS_ALLOWED_ORIGINS",       valueFrom = "${var.secrets_arn}:CORS_ALLOWED_ORIGINS::" }
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        awslogs-group         = aws_cloudwatch_log_group.app.name
        awslogs-region        = data.aws_region.current.name
        awslogs-stream-prefix = "ecs"
      }
    }

    healthCheck = {
      command     = ["CMD-SHELL", "wget -qO- http://localhost:8080/actuator/health || exit 1"]
      interval    = 30
      timeout     = 5
      retries     = 3
      startPeriod = 60
    }
  }])

  tags = { Name = "${var.project}-${var.environment}-task" }
}

# ECS Service com rolling deploy
resource "aws_ecs_service" "app" {
  name                               = "${var.project}-${var.environment}-svc"
  cluster                            = aws_ecs_cluster.main.id
  task_definition                    = aws_ecs_task_definition.app.arn
  desired_count                      = var.desired_count
  launch_type                        = "FARGATE"
  health_check_grace_period_seconds  = 90

  network_configuration {
    subnets          = var.private_subnet_ids
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = var.alb_target_group_arn
    container_name   = "api"
    container_port   = 8080
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  deployment_controller {
    type = "ECS"
  }

  # Ignora mudanças na imagem — o CI/CD atualiza via `aws ecs update-service`
  lifecycle {
    ignore_changes = [task_definition, desired_count]
  }

  tags = { Name = "${var.project}-${var.environment}-svc" }
}
