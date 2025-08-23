# Jenkins EC2 인스턴스에 부여할 IAM 역할
resource "aws_iam_role" "jenkins_role" {
  name = "jenkins-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

# Jenkins 역할에 부여할 정책
resource "aws_iam_policy" "jenkins_policy" {
  name        = "jenkins-policy"
  description = "Policy for Jenkins to interact with AWS services"
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:GetRepositoryPolicy",
          "ecr:DescribeRepositories",
          "ecr:ListImages",
          "ecr:DescribeImages",
          "ecr:BatchGetImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload",
          "ecr:PutImage"
        ],
        Resource = "*"
      },
      {
        Effect = "Allow",
        Action = [
          "ecs:UpdateService",
          "ecs:DescribeServices",
          "ecs:DescribeTaskDefinition",
          "ecs:RegisterTaskDefinition"
        ],
        Resource = "*"
      },
      {
        Effect = "Allow",
        Action = "iam:PassRole",
        Resource = aws_iam_role.ecs_task_execution_role.arn
      },
      {
        Effect = "Allow",
        Action = [
          "s3:ListBucket",
        ],
        Resource = "${aws_s3_bucket.my_bucket.arn}"
      }
      ,{
        Effect = "Allow",
        Action = [
          "s3:PutObject",
          "s3:GetObject",
          "s3:DeleteObject"
        ],
        Resource = "${aws_s3_bucket.my_bucket.arn}/*"
      }
    ]
  })
}

# 역할과 정책 연결
resource "aws_iam_role_policy_attachment" "jenkins_attach" {
  role       = aws_iam_role.jenkins_role.name
  policy_arn = aws_iam_policy.jenkins_policy.arn
}

# EC2 인스턴스 프로파일 생성
resource "aws_iam_instance_profile" "jenkins_profile" {
  name = "jenkins-profile"
  role = aws_iam_role.jenkins_role.name
}

# Jenkins EC2 인스턴스 생성
resource "aws_instance" "jenkins_server" {
  ami           = data.aws_ami.amazon_linux_2.id
  instance_type = "t3.small" # Jenkins는 메모리를 다소 사용하므로 t2.micro보다 t3.small 이상을 권장합니다.
  subnet_id     = aws_subnet.public_a.id
  vpc_security_group_ids = [aws_security_group.jenkins.id]
  iam_instance_profile = aws_iam_instance_profile.jenkins_profile.name
  associate_public_ip_address = true # Public IP 자동 할당
  key_name = "jenkins-key" # 본인의 EC2 키 페어 이름으로 변경하세요.
  root_block_device {
    volume_size = 20 # Jenkins와 Docker 이미지 저장을 위한 최소 크기
    volume_type = "gp2" # 일반 SSD
  }

  # 인스턴스 시작 시 실행할 스크립트 (커스텀 Jenkins 이미지 빌드 및 실행)
  user_data = <<-EOF
              #!/bin/bash
              # swap memory 설정 (Jenkins와 Docker를 위한 메모리 최적화)
              sudo dd if=/dev/zero of=/swapfile bs=128M count=16
              sudo chmod 600 /swapfile
              sudo mkswap /swapfile
              sudo swapon /swapfile
              echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab

              # EC2 인스턴스에 Docker 설치 및 실행
              yum update -y
              amazon-linux-extras install -y docker
              yum install -y getent
              systemctl enable docker
              systemctl start docker
              usermod -a -G docker ec2-user

              # 호스트의 Docker 그룹 GID 가져오기
              DOCKER_GID=$(getent group docker | cut -d: -f3)

              # 커스텀 Dockerfile을 위한 디렉토리 생성
              mkdir /home/ec2-user/jenkins-docker
              cd /home/ec2-user/jenkins-docker

              # AWS CLI와 Docker CLI가 포함된 커스텀 Jenkins Dockerfile 생성
              cat > Dockerfile <<'DOCKERFILE'
FROM jenkins/jenkins:lts-jdk17
USER root

# 빌드 인자로 Docker GID 받기
ARG DOCKER_GID

# 의존성 패키지 설치
RUN apt-get update && apt-get install -y curl unzip lsb-release

# 호스트와 동일한 GID로 docker 그룹을 생성하여 도커 소켓 권한 문제 해결
RUN if getent group $DOCKER_GID; then \
        echo "Group with GID $DOCKER_GID already exists."; \
    else \
        addgroup --gid $DOCKER_GID docker; \
    fi
# jenkins 사용자를 docker 그룹에 추가
RUN usermod -aG $(getent group $DOCKER_GID | cut -d: -f1) jenkins

# Docker CLI 설치
RUN curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
RUN echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian \
  $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
RUN apt-get update && apt-get install -y docker-ce-cli

# AWS CLI v2 설치 (아키텍처 자동 감지)
RUN ARCH=$(uname -m) && \
    if [ "$ARCH" = "x86_64" ]; then \
        curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"; \
    elif [ "$ARCH" = "aarch64" ]; then \
        curl "https://awscli.amazonaws.com/awscli-exe-linux-aarch64.zip" -o "awscliv2.zip"; \
    fi && \
    unzip awscliv2.zip && \
    ./aws/install && \
    rm -rf awscliv2.zip ./aws

# 다시 jenkins 유저로 전환
USER jenkins
DOCKERFILE

              # 커스텀 Jenkins 이미지 빌드
              docker build --build-arg DOCKER_GID=$DOCKER_GID -t custom-jenkins:latest .

              # Jenkins 홈 디렉토리 생성 및 권한 설정
              mkdir -p /var/jenkins_home
              chown -R 1000:1000 /var/jenkins_home

              # 커스텀 Jenkins 컨테이너 실행
              docker run -d \
                -p 8080:8080 \
                -p 50000:50000 \
                -v /var/jenkins_home:/var/jenkins_home \
                -v /var/run/docker.sock:/var/run/docker.sock \
                --name jenkins \
                custom-jenkins:latest
              EOF

  tags = {
    Name = "jenkins-server"
  }
}
