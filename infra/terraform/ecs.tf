# ECS 클러스터 생성
resource "aws_ecs_cluster" "main" {
  name = "main-cluster"
  tags = {
    Name = "main-cluster"
  }
}

# IAM 역할 생성 (ECS Task Execution Role)
resource "aws_iam_role" "ecs_task_execution_role" {
  name = "ecs_task_execution_role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

# IAM 정책 연결 (ECS Task Execution Role)
resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# CloudWatch Log Group 생성 (Main App)
resource "aws_cloudwatch_log_group" "main" {
  name = "/ecs/main-app"
  retention_in_days = 7
  tags = {
    Name = "main-app-log-group"
  }
}

# CloudWatch Log Group 생성 (Emulator App)
resource "aws_cloudwatch_log_group" "emulator" {
  name = "/ecs/emulator-app"
  retention_in_days = 7
  tags = {
    Name = "emulator-app-log-group"
  }
}

# CloudWatch Log Group 생성 (Consumer App)
resource "aws_cloudwatch_log_group" "consumer" {
  name = "/ecs/consumer-app"
  retention_in_days = 7
  tags = {
    Name = "consumer-app-log-group"
  }
}

# ECS 작업 정의 생성 (Main App)
resource "aws_ecs_task_definition" "main" {
  family                   = "main-app-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn

  container_definitions = jsonencode([
    {
      name      = "main-app"
      image     = "${aws_ecr_repository.main.repository_url}:latest"
      cpu       = 256
      memory    = 512
      essential = true
      portMappings = [
        {
          containerPort = 8080
          hostPort      = 8080
        }
      ]
      environment = [
        { name = "DB_HOST", value = aws_db_instance.main.address },
        { name = "DB_NAME", value = var.db_name },
        { name = "DB_USERNAME", value = var.db_username },
        { name = "DB_PASSWORD", value = var.db_password },
        { name = "TZ", value = "Asia/Seoul" },
        { name = "JWT_SECRET", value = var.jwt_secret },
        { name = "JWT_SECRET_BASE64", value = var.jwt_secret_base64 }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group" = aws_cloudwatch_log_group.main.name
          "awslogs-region" = "ap-northeast-2"
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])
}

# ECS 작업 정의 생성 (Emulator App)
resource "aws_ecs_task_definition" "emulator" {
  family                   = "emulator-app-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn

  container_definitions = jsonencode([
    {
      name      = "emulator-app"
      image     = "${aws_ecr_repository.emulator.repository_url}:latest"
      cpu       = 256
      memory    = 512
      essential = true
      portMappings = [
        {
          containerPort = 8081
          hostPort      = 8081
        }
      ]
      environment = [
        { name = "DB_HOST", value = aws_db_instance.main.address },
        { name = "DB_NAME", value = var.db_name },
        { name = "DB_USERNAME", value = var.db_username },
        { name = "DB_PASSWORD", value = var.db_password },
        { name = "KAFKA_BOOTSTRAP_SERVERS", value = "${aws_instance.kafka_server.private_ip}:9092" },
        { name = "TZ", value = "Asia/Seoul" },
        { name = "JWT_SECRET", value = var.jwt_secret },
        { name = "JWT_SECRET_BASE64", value = var.jwt_secret_base64 }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group" = aws_cloudwatch_log_group.emulator.name
          "awslogs-region" = "ap-northeast-2"
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])
}

# ECS 작업 정의 생성 (Consumer App)
resource "aws_ecs_task_definition" "consumer" {
  family                   = "consumer-app-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn

  container_definitions = jsonencode([
    {
      name      = "consumer-app"
      image     = "${aws_ecr_repository.consumer.repository_url}:latest"
      cpu       = 256
      memory    = 512
      essential = true
      portMappings = []
      environment = [
        { name = "DB_HOST", value = aws_db_instance.main.address },
        { name = "DB_NAME", value = var.db_name },
        { name = "DB_USERNAME", value = var.db_username },
        { name = "DB_PASSWORD", value = var.db_password },
        { name = "KAFKA_BOOTSTRAP_SERVERS", value = "${aws_instance.kafka_server.private_ip}:9092" },
        { name = "TZ", value = "Asia/Seoul" },
        { name = "JWT_SECRET", value = var.jwt_secret },
        { name = "JWT_SECRET_BASE64", value = var.jwt_secret_base64 }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group" = aws_cloudwatch_log_group.consumer.name
          "awslogs-region" = "ap-northeast-2"
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])
}

# ECS 서비스 생성 (Main App)
resource "aws_ecs_service" "main" {
  name            = "main-app-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.main.arn
  desired_count   = 1
  launch_type     = "FARGATE"
  health_check_grace_period_seconds = 120

  network_configuration {
    subnets         = [aws_subnet.private_a.id, aws_subnet.private_c.id]
    security_groups = [aws_security_group.ecs_service.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.main.arn
    container_name   = "main-app"
    container_port   = 8080
  }

  depends_on = [aws_lb_listener.https]
}

# ECS 서비스 생성 (Emulator App)
resource "aws_ecs_service" "emulator" {
  name            = "emulator-app-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.emulator.arn
  desired_count   = 2
  launch_type     = "FARGATE"
  health_check_grace_period_seconds = 120

  network_configuration {
    subnets         = [aws_subnet.private_a.id, aws_subnet.private_c.id]
    security_groups = [aws_security_group.ecs_service.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.emulator.arn
    container_name   = "emulator-app"
    container_port   = 8081
  }

  depends_on = [aws_lb_listener_rule.emulator]
}

# ECS 서비스 생성 (Consumer App)
resource "aws_ecs_service" "consumer" {
  name            = "consumer-app-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.consumer.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets         = [aws_subnet.private_a.id, aws_subnet.private_c.id]
    security_groups = [aws_security_group.ecs_service.id]
  }
}

# Auto Scaling 설정 (Main App)
resource "aws_appautoscaling_target" "main" {
  max_capacity       = 2
  min_capacity       = 1
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.main.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "main_cpu" {
  name               = "main-cpu-scaling-policy"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.main.resource_id
  scalable_dimension = aws_appautoscaling_target.main.scalable_dimension
  service_namespace  = aws_appautoscaling_target.main.service_namespace

  target_tracking_scaling_policy_configuration {
    target_value       = 50
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    scale_in_cooldown  = 300
    scale_out_cooldown = 60
  }
}

# Auto Scaling 설정 (Emulator App)
resource "aws_appautoscaling_target" "emulator" {
  max_capacity       = 4
  min_capacity       = 2
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.emulator.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "emulator_cpu" {
  name               = "emulator-cpu-scaling-policy"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.emulator.resource_id
  scalable_dimension = aws_appautoscaling_target.emulator.scalable_dimension
  service_namespace  = aws_appautoscaling_target.emulator.service_namespace

  target_tracking_scaling_policy_configuration {
    target_value       = 50
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    scale_in_cooldown  = 300
    scale_out_cooldown = 60
  }
}

# Auto Scaling 설정 (Consumer App)
resource "aws_appautoscaling_target" "consumer" {
  max_capacity       = 2
  min_capacity       = 1
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.consumer.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "consumer_cpu" {
  name               = "consumer-cpu-scaling-policy"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.consumer.resource_id
  scalable_dimension = aws_appautoscaling_target.consumer.scalable_dimension
  service_namespace  = aws_appautoscaling_target.consumer.service_namespace

  target_tracking_scaling_policy_configuration {
    target_value       = 50
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    scale_in_cooldown  = 300
    scale_out_cooldown = 60
  }
}