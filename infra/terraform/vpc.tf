# VPC 생성
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = {
    Name = "main-vpc"
  }
}

# Public Subnet 2개 생성 (가용 영역 a, c)
resource "aws_subnet" "public_a" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.1.0/24"
  availability_zone = "ap-northeast-2a"
  tags = {
    Name = "public-subnet-a"
  }
}

resource "aws_subnet" "public_c" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.2.0/24"
  availability_zone = "ap-northeast-2c"
  tags = {
    Name = "public-subnet-c"
  }
}

# Private Subnet 2개 생성 (가용 영역 a, c)
resource "aws_subnet" "private_a" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.3.0/24"
  availability_zone = "ap-northeast-2a"
  tags = {
    Name = "private-subnet-a"
  }
}

resource "aws_subnet" "private_c" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.4.0/24"
  availability_zone = "ap-northeast-2c"
  tags = {
    Name = "private-subnet-c"
  }
}

# 인터넷 게이트웨이 생성
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
  tags = {
    Name = "main-igw"
  }
}

# Public 라우팅 테이블 생성
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name = "public-route-table"
  }
}

# Public 라우팅 테이블과 서브넷 연결
resource "aws_route_table_association" "public_a" {
  subnet_id      = aws_subnet.public_a.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "public_c" {
  subnet_id      = aws_subnet.public_c.id
  route_table_id = aws_route_table.public.id
}

# EIP for NAT Gateway
# resource "aws_eip" "nat" {
#   domain = "vpc"
#   depends_on = [aws_internet_gateway.main]
# }

# # NAT Gateway
# resource "aws_nat_gateway" "main" {
#   allocation_id = aws_eip.nat.id
#   subnet_id     = aws_subnet.public_a.id
#   depends_on    = [aws_internet_gateway.main]
#   tags = {
#     Name = "main-nat-gateway"
#   }
# }

# Private 라우팅 테이블 생성
resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id

  # route {
  #   cidr_block     = "0.0.0.0/0"
  #   nat_gateway_id = aws_nat_gateway.main.id
  # }

  tags = {
    Name = "private-route-table"
  }
}

# Private 라우팅 테이블과 서브넷 연결
resource "aws_route_table_association" "private_a_assoc" {
  subnet_id      = aws_subnet.private_a.id
  route_table_id = aws_route_table.private.id
}

resource "aws_route_table_association" "private_c_assoc" {
  subnet_id      = aws_subnet.private_c.id
  route_table_id = aws_route_table.private.id
}
