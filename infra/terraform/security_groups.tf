# ALB 보안 그룹 (HTTP 및 HTTPS 트래픽 허용)
resource "aws_security_group" "alb" {
  name        = "alb-sg"
  description = "Allow HTTP and HTTPS inbound traffic"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "alb-sg"
  }
}

# ECS 서비스 보안 그룹 (ALB로부터의 트래픽만 허용)
resource "aws_security_group" "ecs_service" {
  name        = "ecs-service-sg"
  description = "Allow traffic from ALB, self, and Jenkins"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 8080 # Spring Boot App Port
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  ingress {
    protocol  = "tcp"
    from_port = 0
    to_port   = 65535
    self      = true # VPC 엔드포인트 통신을 위해 자체 트래픽 허용
  }

  ingress {
    from_port       = 443 # HTTPS for AWS APIs from Jenkins
    to_port         = 443
    protocol        = "tcp"
    security_groups = [aws_security_group.jenkins.id] # Jenkinsからのアクセスを許可
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "ecs-service-sg"
  }
}

# RDS 보안 그룹 (ECS 서비스와 Bastion 서버로부터의 트래픽만 허용)
resource "aws_security_group" "rds" {
  name        = "rds-sg"
  description = "Allow traffic from ECS service and Bastion Server"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 3306 # MySQL Port
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs_service.id, aws_security_group.bastion.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "rds-sg"
  }
}

# Bastion 서버 보안 그룹 (SSH 트래픽 허용)
resource "aws_security_group" "bastion" {
  name        = "bastion-sg"
  description = "Allow SSH inbound traffic"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # 보안을 위해 실제 IP 주소로 변경하세요.
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "bastion-sg"
  }
}