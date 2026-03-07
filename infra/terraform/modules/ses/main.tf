# Verificação de domínio no SES
# Após o apply, adicione os registros DNS gerados no seu provedor de domínio.
resource "aws_ses_domain_identity" "main" {
  domain = var.mail_domain
}

resource "aws_ses_domain_dkim" "main" {
  domain = aws_ses_domain_identity.main.domain
}

# Política IAM para envio via SMTP (usada pelo Spring Mail)
resource "aws_iam_user" "ses_smtp" {
  name = "${var.project}-${var.environment}-ses-smtp"
  tags = { Name = "${var.project}-${var.environment}-ses-smtp" }
}

resource "aws_iam_user_policy" "ses_send" {
  name = "ses-send-email"
  user = aws_iam_user.ses_smtp.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["ses:SendEmail", "ses:SendRawEmail"]
      Resource = "*"
    }]
  })
}

resource "aws_iam_access_key" "ses_smtp" {
  user = aws_iam_user.ses_smtp.name
}
