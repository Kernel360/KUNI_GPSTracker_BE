# Bastion EC2 인스턴스 생성
resource "aws_instance" "bastion_server" {
  ami           = data.aws_ami.amazon_linux_2.id
  instance_type = "t3.micro"
  subnet_id     = aws_subnet.public_a.id
  vpc_security_group_ids = [aws_security_group.bastion.id]
  associate_public_ip_address = true
  key_name = "jenkins-key" # 본인의 EC2 키 페어 이름으로 변경하세요.
  root_block_device {
    volume_size = 20 # 20GB로 설정, 필요에 따라 조정
    volume_type = "gp2"
  }
  user_data = <<-EOF
              #!/bin/bash
              yum update -y
              yum install -y mysql

              # swap memory 설정 (Jenkins와 Docker를 위한 메모리 최적화)
              sudo dd if=/dev/zero of=/swapfile bs=128M count=16
              sudo chmod 600 /swapfile
              sudo mkswap /swapfile
              sudo swapon /swapfile
              echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab

              # EC2 인스턴스에 Docker 설치 및 실행
              amazon-linux-extras install -y docker
              service docker start
              usermod -a -G docker ec2-user

              EOF

  tags = {
    Name = "bastion-server"
  }
}