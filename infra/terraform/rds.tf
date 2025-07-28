
# RDS 서브넷 그룹 생성
resource "aws_db_subnet_group" "main" {
  name       = "main-db-subnet-group"
  subnet_ids = [aws_subnet.private_a.id, aws_subnet.private_c.id]

  tags = {
    Name = "Main DB subnet group"
  }
}

# RDS DB 인스턴스 생성
resource "aws_db_instance" "main" {
  allocated_storage    = 20
  storage_type         = "gp2"
  engine               = "mysql"
  engine_version       = "8.0"
  instance_class       = "db.t3.micro"
  db_name              = "gpsTracker"
  username             = "admin"
  password             = "password" # 실제 환경에서는 Secrets Manager 또는 파라미터 스토어 사용 권장
  db_subnet_group_name = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  skip_final_snapshot  = true
}
