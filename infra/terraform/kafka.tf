# Kafka 브로커 EC2 인스턴스
resource "aws_instance" "kafka_server" {
  ami           = data.aws_ami.amazon_linux_2.id
  instance_type = "t3.micro"
  subnet_id     = aws_subnet.private_a.id
  vpc_security_group_ids = [aws_security_group.kafka.id]
  key_name      = "jenkins-key"
  root_block_device {
    volume_size = 20 # 20GB로 설정, 필요에 따라 조정
    volume_type = "gp2"
  }
  user_data = <<-EOF
            #!/bin/bash
            yum update -y

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

            # Docker Compose 설치
            sudo yum install -y git
            sudo curl -SL https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-linux-x86_64 -o /usr/local/bin/docker-compose
            sudo chmod +x /usr/local/bin/docker-compose 


            mkdir -p ~/kafka-ec2 && cd ~/kafka-ec2
            

            PRIVATE_IP=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4)
            echo "PRIVATE_IP=$PRIVATE_IP" > .env

            cd ~/kafka-ec2
            # docker compose down -v --remove-orphans || true
            # rm -f docker-compose.yml
            cat > docker-compose.yml <<'YAML'
services:
  zookeeper:
    image: bitnami/zookeeper:3.9
    container_name: zookeeper
    environment:
      - ALLOW_ANONYMOUS_LOGIN=yes
      - ZOO_HEAP_SIZE=256          # ZK 메모리 축소
    ports:
      - "2181:2181"
    volumes:
      - zookeeper_data:/bitnami/zookeeper
    restart: unless-stopped

  kafka1:
    image: bitnami/kafka:3.6
    container_name: kafka1
    hostname: kafka1
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"    # 외부(호스트) 접속
      # 내부용 19092는 도커 네트워크로만 쓰니 굳이 노출 불필요
    environment:
      - KAFKA_BROKER_ID=1
      - KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181
      - ALLOW_PLAINTEXT_LISTENER=yes
      - KAFKA_HEAP_OPTS=-Xms256m -Xmx512m   # ★ JVM 힙 축소(저메모리 인스턴스용)
      - KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CLIENT:PLAINTEXT,EXTERNAL:PLAINTEXT
      - KAFKA_CFG_LISTENERS=CLIENT://:19092,EXTERNAL://:9092
      - KAFKA_CFG_ADVERTISED_LISTENERS=CLIENT://kafka1:19092,EXTERNAL://$${PRIVATE_IP}:9092
      - KAFKA_INTER_BROKER_LISTENER_NAME=CLIENT
      # 단일 브로커이므로 복제 관련 값은 전부 1
      - KAFKA_CFG_DEFAULT_REPLICATION_FACTOR=1
      - KAFKA_CFG_OFFSETS_TOPIC_REPLICATION_FACTOR=1
      - KAFKA_CFG_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1
      - KAFKA_CFG_TRANSACTION_STATE_LOG_MIN_ISR=1
      - KAFKA_CFG_MIN_INSYNC_REPLICAS=1
      - KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=false
    volumes:
      - kafka1_data:/bitnami/kafka
    restart: unless-stopped

volumes:
  zookeeper_data:
  kafka1_data:
YAML
            # Docker Compose 실행
            docker-compose pull
            docker-compose up -d

            # Kafka가 완전히 시작될 때까지 대기
            until docker exec kafka1 kafka-topics.sh --bootstrap-server kafka1:19092 --list >/dev/null 2>&1; do
                echo "Waiting for Kafka to start..."
                sleep 5
            done

            # Kafka 토픽 생성
            docker exec kafka1 kafka-topics.sh \
                --bootstrap-server kafka1:19092 \
                --create \
                --topic mdn-topic \
                --replication-factor 1 \
                --partitions 3

              EOF

  tags = {
    Name = "kafka-server"
  }
}
