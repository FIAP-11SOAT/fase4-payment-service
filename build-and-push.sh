#!/bin/bash

set -e

# Configurações
AWS_REGION="us-east-1"
PROJECT_NAME="fase4-payment-service"
ECR_REPOSITORY_NAME="${PROJECT_NAME}-ecr"
IMAGE_TAG="${1:-latest}"

echo "=========================================="
echo "Build and Push Docker Image to AWS ECR"
echo "=========================================="
echo "Region: ${AWS_REGION}"
echo "Repository: ${ECR_REPOSITORY_NAME}"
echo "Image Tag: ${IMAGE_TAG}"
echo "=========================================="

# Obter Account ID da AWS
echo "🔍 Obtendo AWS Account ID..."
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
echo "✅ Account ID: ${AWS_ACCOUNT_ID}"

# Construir URL completa do ECR
ECR_URL="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
FULL_IMAGE_NAME="${ECR_URL}/${ECR_REPOSITORY_NAME}:${IMAGE_TAG}"

# Fazer login no ECR
echo ""
echo "🔐 Fazendo login no AWS ECR..."
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_URL}
echo "✅ Login realizado com sucesso!"

# Build da imagem Docker
echo ""
echo "🏗️  Construindo imagem Docker..."
docker build -t ${ECR_REPOSITORY_NAME}:${IMAGE_TAG} -t ${FULL_IMAGE_NAME} --target production .
echo "✅ Imagem construída com sucesso!"

# Push da imagem para o ECR
echo ""
echo "📤 Fazendo push da imagem para o ECR..."
docker push ${FULL_IMAGE_NAME}
echo "✅ Imagem enviada com sucesso!"

# Também taguear e enviar como 'latest' se não for já 'latest'
if [ "${IMAGE_TAG}" != "latest" ]; then
  echo ""
  echo "🏷️  Tagueando também como 'latest'..."
  FULL_IMAGE_NAME_LATEST="${ECR_URL}/${ECR_REPOSITORY_NAME}:latest"
  docker tag ${FULL_IMAGE_NAME} ${FULL_IMAGE_NAME_LATEST}
  docker push ${FULL_IMAGE_NAME_LATEST}
  echo "✅ Tag 'latest' enviada com sucesso!"
fi

echo ""
echo "=========================================="
echo "✅ Processo concluído com sucesso!"
echo "=========================================="
echo "Imagem disponível em:"
echo "${FULL_IMAGE_NAME}"
if [ "${IMAGE_TAG}" != "latest" ]; then
  echo "${ECR_URL}/${ECR_REPOSITORY_NAME}:latest"
fi
echo "=========================================="

