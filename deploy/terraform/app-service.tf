resource "aws_ecs_service" "service" {
  name            = "${local.project_name}-service"
  cluster         = data.aws_ecs_cluster.ecs_cluster.id
  task_definition = aws_ecs_task_definition.service_task.arn
  desired_count   = 2
  launch_type     = "FARGATE"
  network_configuration {
    subnets          = data.aws_subnets.private_subnets.ids
    security_groups  = [aws_security_group.ecs_sg.id]
    assign_public_ip = false
  }
  load_balancer {
    target_group_arn = aws_lb_target_group.service_tg.arn
    container_name   = "${local.project_name}-container"
    container_port   = 8080
  }
  depends_on = [aws_lb_listener_rule.service_rule]

  tags = {
    Name = "${local.project_name}-service"
  }
}

resource "aws_ecs_task_definition" "service_task" {
  family                   = "${local.project_name}-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]

  # Combinações válidas Fargate (exemplo comum e barato)
  cpu    = "512" # 0.25 vCPU
  memory = "1024"   # 512 MiB (mínimo para 256 cpu)

  execution_role_arn = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn      = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "${local.project_name}-container"
      image     = "${aws_ecr_repository.ecs_repository.repository_url}:latest" # ou use tag fixa!
      essential = true

      portMappings = [
        {
          protocol      = "tcp"
          containerPort = 8080
          hostPort      = 8080 # OBRIGATÓRIO ser igual no Fargate
        }
      ]

      environment = [] # ou suas variáveis aqui

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.app_log_group.name
          "awslogs-region"        = data.aws_region.current.region
          "awslogs-stream-prefix" = "ecs"
        }
      }

      # Recomendado para ALB
      healthCheck = {
        command     = ["CMD-SHELL", "curl -f http://localhost:8080/health || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }
    }
  ])

  tags = {
    Name = "${local.project_name}-task"
  }
}