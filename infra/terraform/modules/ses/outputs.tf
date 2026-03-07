output "dkim_tokens"        { value = aws_ses_domain_dkim.main.dkim_tokens }
output "smtp_access_key_id" { value = aws_iam_access_key.ses_smtp.id; sensitive = true }
output "smtp_secret"        { value = aws_iam_access_key.ses_smtp.ses_smtp_password_v4; sensitive = true }
