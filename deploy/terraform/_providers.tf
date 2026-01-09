provider "aws" {
  region = local.aws_region

  default_tags {
    tags = {
      Team      = "mfava"
      Project   = local.project_name
      Terraform = "true"
    }
  }
}