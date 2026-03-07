# Security Group — permite acesso apenas das subnets privadas (ECS)
resource "aws_security_group" "rds" {
  name        = "${var.project}-${var.environment}-rds-sg"
  description = "Acesso ao RDS PostgreSQL — somente ECS"
  vpc_id      = var.vpc_id

  ingress {
    description = "PostgreSQL from private subnets"
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project}-${var.environment}-rds-sg" }
}

resource "aws_db_subnet_group" "main" {
  name       = "${var.project}-${var.environment}-db-subnet"
  subnet_ids = var.private_subnet_ids
  tags       = { Name = "${var.project}-${var.environment}-db-subnet" }
}

resource "aws_db_instance" "postgres" {
  identifier        = "${var.project}-${var.environment}"
  engine            = "postgres"
  engine_version    = "16"
  instance_class    = var.instance_class
  allocated_storage = 20
  storage_type      = "gp3"
  storage_encrypted = true

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  # Multi-AZ em produção — falha automática para réplica em outra AZ
  multi_az = var.multi_az

  backup_retention_period = 7
  backup_window           = "03:00-04:00"
  maintenance_window      = "Mon:04:00-Mon:05:00"

  deletion_protection       = true
  skip_final_snapshot       = false
  final_snapshot_identifier = "${var.project}-${var.environment}-final-snapshot"

  performance_insights_enabled = true

  tags = { Name = "${var.project}-${var.environment}-rds" }
}
