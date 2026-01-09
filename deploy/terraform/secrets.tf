resource "aws_secretsmanager_secret" "secrets" {
  name                    = "${local.project_name}-secrets"
  description             = "Secrets for ${local.project_name} project"
  recovery_window_in_days = 0

  tags = {
    Name = "${local.project_name}-secrets"
  }
}

locals {
  gateway_endpoint = trimsuffix(local.aws_infra_secrets["GTW_ENDPOINT"], "/")
}

resource "aws_secretsmanager_secret_version" "secrets" {
  secret_id     = aws_secretsmanager_secret.secrets.id
  secret_string = jsonencode({
    "fase4.payment.service.marcadopago.webhookUrl" = "${local.gateway_endpoint}/payment/webhook"
    "fase4.payment.service.marcadopago.accessToken" = local.aws_master_secrets["MERCADO_PAGO_TOKEN"]
    "fase4.payment.service.marcadopago.userID" = local.aws_master_secrets["MERCADO_PAGO_USER_ID"]
    "fase4.payment.service.marcadopago.externalPosID" = local.aws_master_secrets["MERCADO_PAGO_EXTERNAL_POS_ID"]
  })
}
