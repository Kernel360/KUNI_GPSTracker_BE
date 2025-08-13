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

output "bastion_server_public_ip" {
  description = "The public IP address of the Bastion server."
  value       = aws_instance.bastion_server.public_ip
}

# output "k6_monitoring_public_ip" {
#   description = "The public IP address of the k6 monitoring EC2 instance"
#   value       = aws_instance.k6_monitoring.public_ip
# }

# output "k6_monitoring_public_dns" {
#   description = "The public DNS of the k6 monitoring EC2 instance"
#   value       = aws_instance.k6_monitoring.public_dns
# }

output "kafka_private_ip" {
  description = "The private IP address of the Kafka server."
  value       = aws_instance.kafka.private_ip
}
