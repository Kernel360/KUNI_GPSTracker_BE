# ECR 리포지토리 생성
resource "aws_ecr_repository" "main" {
  name = "main-app"
  tags = {
    Name = "main-app-repo"
  }
}

# ECR 리포지토리 생성 (Emulator App)
resource "aws_ecr_repository" "emulator" {
  name = "emulator-app"
  tags = {
    Name = "emulator-app-repo"
  }
}

# ECR 리포지토리 생성 (Consumer App)
resource "aws_ecr_repository" "consumer" {
  name = "consumer-app"
  tags = {
    Name = "consumer-app-repo"
  }
}