terraform {
  required_version = ">= 1.11.0"

  backend "s3" {
    bucket = "fase4-terraform-state"
    key    = "fase4-order-service/terraform.tfstate"
    region = "us-east-1"
  }

  required_providers {

    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.14.1"
    }

    random = {
      source  = "hashicorp/random"
      version = "~> 3.7.2"
    }

  }
}