# resource "aws_instance" "k6_monitoring" {
#   ami           = "ami-0e9bfdb247cc8de84" # Ubuntu Server 22.04 LTS (HVM), ap-northeast-2
#   instance_type = "t3.micro" # Adjust instance type as needed
#   key_name      = "jenkins-key" # Assuming you have a key pair named jenkins-key
#   subnet_id     = aws_subnet.public_a.id # Explicitly assign to a public subnet within the main VPC
#   associate_public_ip_address = true # Public IP 자동 할당
#   vpc_security_group_ids = [aws_security_group.k6_monitoring_sg.id] # Referencing a new security group
#   root_block_device {
#     volume_size = 20 # Jenkins와 Docker 이미지 저장을 위한 최소 크기
#     volume_type = "gp2" # 일반 SSD
#   }

#   tags = {
#     Name = "k6-InfluxDB-Grafana-Monitoring"
#   }

#   user_data = <<-EOF
#               #!/bin/bash
#               # # Set up swap memory
#               # sudo dd if=/dev/zero of=/swapfile bs=128M count=16
#               # sudo chmod 600 /swapfile
#               # sudo mkswap /swapfile
#               # sudo swapon /swapfile
#               # echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab

#               # Update and install dependencies
#               sudo apt-get update -y
#               sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common

#               # Install Docker
#               curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
#               echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
#               sudo apt-get update -y
#               sudo apt-get install -y docker-ce docker-ce-cli containerd.io
#               sudo systemctl start docker
#               sudo systemctl enable docker
#               sudo usermod -aG docker ubuntu

#               # Install Docker Compose
#               sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
#               sudo chmod +x /usr/local/bin/docker-compose

#               # Create and run containers
#               mkdir -p /home/ubuntu/monitoring
#               cd /home/ubuntu/monitoring

#               cat > docker-compose.yml << 'DOCKER_COMPOSE_YML'
# version: '3.8'

# services:
#   influxdb:
#     image: influxdb:2.7
#     container_name: influxdb
#     ports:
#       - "8086:8086"
#     volumes:
#       - influxdb_data:/var/lib/influxdb2
#     environment:
#       - DOCKER_INFLUXDB_INIT_MODE=setup
#       - DOCKER_INFLUXDB_INIT_USERNAME=k6_user
#       - DOCKER_INFLUXDB_INIT_PASSWORD=k6_password
#       - DOCKER_INFLUXDB_INIT_ORG=k6_org
#       - DOCKER_INFLUXDB_INIT_BUCKET=k6
#       - DOCKER_INFLUXDB_INIT_ADMIN_TOKEN=k6_token
#     restart: unless-stopped

#   grafana:
#     image: grafana/grafana:latest
#     container_name: grafana
#     ports:
#       - "3000:3000"
#     volumes:
#       - grafana_data:/var/lib/grafana
#     environment:
#       - GF_SECURITY_ADMIN_USER=admin
#       - GF_SECURITY_ADMIN_PASSWORD=grafana
#     depends_on:
#       - influxdb
#     restart: unless-stopped

# volumes:
#   influxdb_data: {}
#   grafana_data: {}
# DOCKER_COMPOSE_YML

#               # Set ownership to ubuntu user
#               chown -R ubuntu:ubuntu /home/ubuntu/monitoring

#               # Run docker-compose
#               sudo /usr/local/bin/docker-compose up -d
#               EOF
# }

# resource "aws_security_group" "k6_monitoring_sg" {
#   name        = "k6-monitoring-security-group"
#   description = "Allow inbound traffic for k6 monitoring (Grafana, InfluxDB, SSH)"
#   vpc_id      = aws_vpc.main.id # Assuming your main VPC is named 'main'

#   ingress {
#     description = "SSH from anywhere"
#     from_port   = 22
#     to_port     = 22
#     protocol    = "tcp"
#     cidr_blocks = ["0.0.0.0/0"]
#   }

#   ingress {
#     description = "xk6 monitoring access from anywhere"
#     from_port = 80
#     to_port = 80
#     protocol = "tcp"
#     cidr_blocks = ["0.0.0.0/0"]
#   }

#   ingress {
#     description = "Grafana access from anywhere"
#     from_port   = 3000
#     to_port     = 3000
#     protocol    = "tcp"
#     cidr_blocks = ["0.0.0.0/0"] # Consider restricting this to your specific IP addresses for better security
#   }

#   ingress {
#     description = "InfluxDB access from anywhere"
#     from_port   = 8086
#     to_port     = 8086
#     protocol    = "tcp"
#     cidr_blocks = ["0.0.0.0/0"] # Consider restricting this to your specific IP addresses for better security
#   }

#   egress {
#     from_port   = 0
#     to_port     = 0
#     protocol    = "-1"
#     cidr_blocks = ["0.0.0.0/0"]
#   }

#   tags = {
#     Name = "k6-Monitoring-SG"
#   }
# }
