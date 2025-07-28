
# ECR 리포지토리 생성
resource "aws_ecr_repository" "main" {
  name = "spring-app"
  tags = {
    Name = "spring-app-repo"
  }
}
