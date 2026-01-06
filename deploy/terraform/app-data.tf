data "aws_ecs_cluster" "ecs_cluster" {
  cluster_name = "fase4-infra-microservices-ecs-cluster"
}

data "aws_security_group" "alb_sg" {
  filter {
    name   = "group-name"
    values = ["fase4-infra-microservices-ecs-alb-sg"]
  }
}


data "aws_vpc" "existing" {
  id = local.aws_infra_secrets["VPC_ID"]
}

data "aws_subnets" "public_subnets" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.existing.id]
  }
  filter {
    name   = "tag:Tier"
    values = ["public"]
  }
}

data "aws_subnets" "private_subnets" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.existing.id]
  }
  filter {
    name   = "tag:Tier"
    values = ["private"]
  }
}