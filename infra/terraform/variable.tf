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

variable "s3_bucket_name" {
  description = "S3 버킷 이름"
  type        = string
}

variable "jwt_secret" {
  description = "JWT 토큰 생성/검증에 사용하는 비밀 키 (일반 문자열)"
  type        = string
  sensitive   = true
}

variable "jwt_secret_base64" {
  description = "JWT 토큰 생성/검증에 사용하는 Base64 인코딩된 비밀 키"
  type        = string
  sensitive   = true
}