variable "project"     { type = string }
variable "environment" { type = string }
variable "mail_host"   { type = string }
variable "cors_origins" { type = string }

variable "db_url" {
  type      = string
  sensitive = true
}

variable "db_username" {
  type      = string
  sensitive = true
}

variable "db_password" {
  type      = string
  sensitive = true
}

variable "jwt_secret" {
  type      = string
  sensitive = true
}

variable "mail_username" {
  type      = string
  sensitive = true
  default   = ""
}

variable "mail_password" {
  type      = string
  sensitive = true
  default   = ""
}
