
# Bastion EC2 인스턴스 생성
resource "aws_instance" "bastion_server" {
  ami           = data.aws_ami.amazon_linux_2.id
  instance_type = "t2.micro"
  subnet_id     = aws_subnet.public_a.id
  vpc_security_group_ids = [aws_security_group.bastion.id]
  associate_public_ip_address = true
  key_name = "jenkins-key" # 본인의 EC2 키 페어 이름으로 변경하세요.

  tags = {
    Name = "bastion-server"
  }
}
