variable "project"               { type = string }
variable "environment"           { type = string }
variable "vpc_id"                { type = string }
variable "private_subnet_ids"    { type = list(string) }
variable "ecr_repository_url"    { type = string }
variable "image_tag"             { type = string }
variable "alb_target_group_arn"  { type = string }
variable "alb_security_group_id" { type = string }
variable "rds_security_group_id" { type = string }
variable "secrets_arn"           { type = string }
variable "task_cpu"              { type = number; default = 512 }
variable "task_memory"           { type = number; default = 1024 }
variable "desired_count"         { type = number; default = 1 }
