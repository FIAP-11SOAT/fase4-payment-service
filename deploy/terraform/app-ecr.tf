resource "aws_ecr_repository" "ecs_repository" {
  name                 = "${local.project_name}-ecr"
  image_tag_mutability = "MUTABLE"
  image_scanning_configuration {
    scan_on_push = true
  }
}