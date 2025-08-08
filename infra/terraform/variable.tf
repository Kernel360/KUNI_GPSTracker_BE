variable "domain_name" {
  description = "Route 53에서 관리하는 루트 도메인 이름"
  type        = string
}

variable "db_username" {
  description = "RDS 데이터베이스 사용자 이름"
  type        = string
}

variable "db_password" {
  description = "RDS 데이터베이스 사용자 비밀번호"
  type        = string
  sensitive   = true
}

variable "db_name" {
  description = "RDS 데이터베이스 이름"
  type        = string
}
