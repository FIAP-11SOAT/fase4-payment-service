resource "aws_lb_target_group" "service_tg" {
  name        = "${local.project_name}-tg"
  port        = 8080
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = data.aws_vpc.existing.id
  health_check {
    path                = "/health"
    protocol            = "HTTP"
    matcher             = "200"
    port                = 8080
    healthy_threshold   = 2
    unhealthy_threshold = 2
    timeout             = 10
    interval            = 30
  }
}

resource "aws_lb_listener_rule" "service_rule" {
  listener_arn = local.aws_infra_secrets["ALB_LISTENER_ARN"]
  priority     = 10
  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.service_tg.arn
  }
  condition {
    path_pattern {
      values = ["/order*", "/order/*"]
    }
  }
}