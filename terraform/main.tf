# ── ECR Repository ─────────────────────────────────────────────────────────────
resource "aws_ecr_repository" "java_web_app" {
  name                 = var.repository_name
  image_tag_mutability = var.image_tag_mutability

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Project     = "docker-training"
    ManagedBy   = "terraform"
    Environment = "dev"
  }
}

# ── Lifecycle Policy — keep only the last 10 images to save storage ───────────
resource "aws_ecr_lifecycle_policy" "cleanup" {
  repository = aws_ecr_repository.java_web_app.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep only last 10 images"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 10
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}
