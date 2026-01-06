resource "aws_cloudwatch_log_group" "app_log_group" {
  name              = "/ecs/${local.project_name}"
  retention_in_days = 30

  tags = {
    Name = "${local.project_name}-app-log-group"
  }
}