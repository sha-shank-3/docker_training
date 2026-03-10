output "ecr_repository_url" {
  description = "The URL of the ECR repository"
  value       = aws_ecr_repository.java_web_app.repository_url
}

output "ecr_repository_arn" {
  description = "The ARN of the ECR repository"
  value       = aws_ecr_repository.java_web_app.arn
}

output "ecr_registry_id" {
  description = "The registry ID (AWS Account ID)"
  value       = aws_ecr_repository.java_web_app.registry_id
}
