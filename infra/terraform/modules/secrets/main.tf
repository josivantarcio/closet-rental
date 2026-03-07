# Todos os segredos da aplicação em um único Secret Manager secret (JSON)
# O ECS injeta as variáveis individualmente via valueFrom

resource "aws_secretsmanager_secret" "app" {
  name                    = "${var.project}/${var.environment}/app"
  description             = "Segredos da aplicação Closet Rental"
  recovery_window_in_days = 7

  tags = { Name = "${var.project}-${var.environment}-secrets" }
}

resource "aws_secretsmanager_secret_version" "app" {
  secret_id = aws_secretsmanager_secret.app.id

  secret_string = jsonencode({
    SPRING_DATASOURCE_URL      = var.db_url
    SPRING_DATASOURCE_USERNAME = var.db_username
    SPRING_DATASOURCE_PASSWORD = var.db_password
    APP_JWT_SECRET             = var.jwt_secret
    MAIL_HOST                  = var.mail_host
    MAIL_USERNAME              = var.mail_username
    MAIL_PASSWORD              = var.mail_password
    CORS_ALLOWED_ORIGINS       = var.cors_origins
  })

  lifecycle {
    ignore_changes = [secret_string]
  }
}
