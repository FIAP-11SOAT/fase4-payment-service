resource "aws_dynamodb_table" "payments" {
  name         = "${local.project_name}-payments"
  billing_mode = "PAY_PER_REQUEST"

  hash_key = "id"

  attribute {
    name = "id"
    type = "S"
  }

  tags = {
    Name        = "${local.project_name}-users"
  }
}