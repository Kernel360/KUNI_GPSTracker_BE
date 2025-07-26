
output "alb_dns_name" {
  description = "The DNS name of the load balancer."
  value       = aws_lb.main.dns_name
}

output "ecr_repository_url" {
  description = "The URL of the ECR repository."
  value       = aws_ecr_repository.main.repository_url
}

output "rds_endpoint" {
  description = "The endpoint of the RDS instance."
  value       = aws_db_instance.main.endpoint
}

output "jenkins_server_public_ip" {
  description = "The public IP address of the Jenkins server."
  value       = aws_instance.jenkins_server.public_ip
}
