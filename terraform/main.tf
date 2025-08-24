# =============================================================================
# Terraform Configuration for iRoom Backend Infrastructure
# =============================================================================
# 이 설정은 AWS에서 완전한 CI/CD 인프라를 생성합니다:
# - EC2 인스턴스 (Ubuntu 24.04)
# - ECR 리포지토리
# - CodeDeploy 애플리케이션 및 배포 그룹
# - IAM 역할 및 정책
# - 보안 그룹
# - 로드 밸런서 (옵션)
# =============================================================================

terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# =============================================================================
# Variables
# =============================================================================

variable "project_name" {
  description = "프로젝트 이름"
  type        = string
  default     = "iroom-backend"
}

variable "environment" {
  description = "환경 (dev, staging, prod)"
  type        = string
  default     = "prod"
}

variable "instance_type" {
  description = "EC2 인스턴스 타입"
  type        = string
  default     = "t3.medium"
}

variable "key_name" {
  description = "EC2 키 페어 이름"
  type        = string
}

variable "allowed_cidr_blocks" {
  description = "접근을 허용할 CIDR 블록들"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "github_repo_url" {
  description = "GitHub 리포지토리 URL"
  type        = string
}

# =============================================================================
# Data Sources
# =============================================================================

data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-noble-24.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# =============================================================================
# VPC and Networking
# =============================================================================

resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name        = "${var.project_name}-vpc"
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name        = "${var.project_name}-igw"
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_subnet" "public" {
  count = 2

  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.${count.index + 1}.0/24"
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = true

  tags = {
    Name        = "${var.project_name}-public-subnet-${count.index + 1}"
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name        = "${var.project_name}-public-rt"
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_route_table_association" "public" {
  count = length(aws_subnet.public)

  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

# =============================================================================
# Security Groups
# =============================================================================

resource "aws_security_group" "web_server" {
  name        = "${var.project_name}-web-server"
  description = "Security group for web server"
  vpc_id      = aws_vpc.main.id

  # HTTP
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_blocks
  }

  # HTTPS
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_blocks
  }

  # SSH
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_blocks
  }

  # Spring Boot Development (임시)
  ingress {
    from_port   = 3055
    to_port     = 3055
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_blocks
  }

  # Blue-Green Ports (임시)
  ingress {
    from_port   = 8080
    to_port     = 8081
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_blocks
  }

  # Monitoring (Node Exporter)
  ingress {
    from_port   = 9100
    to_port     = 9100
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"]
  }

  # All outbound traffic
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.project_name}-web-server-sg"
    Environment = var.environment
    Project     = var.project_name
  }
}

# =============================================================================
# IAM Roles and Policies
# =============================================================================

# EC2 인스턴스 역할
resource "aws_iam_role" "ec2_role" {
  name = "${var.project_name}-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name        = "${var.project_name}-ec2-role"
    Environment = var.environment
    Project     = var.project_name
  }
}

# CodeDeploy 에이전트를 위한 정책
resource "aws_iam_role_policy" "ec2_codedeploy_policy" {
  name = "${var.project_name}-ec2-codedeploy-policy"
  role = aws_iam_role.ec2_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:ListBucket"
        ]
        Resource = [
          "arn:aws:s3:::aws-codedeploy-ap-northeast-2/*",
          "arn:aws:s3:::aws-codedeploy-ap-northeast-2"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams"
        ]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_instance_profile" "ec2_profile" {
  name = "${var.project_name}-ec2-profile"
  role = aws_iam_role.ec2_role.name
}

# CodeDeploy 서비스 역할
resource "aws_iam_role" "codedeploy_role" {
  name = "${var.project_name}-codedeploy-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "codedeploy.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name        = "${var.project_name}-codedeploy-role"
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_iam_role_policy_attachment" "codedeploy_role_attachment" {
  role       = aws_iam_role.codedeploy_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSCodeDeployRole"
}

# =============================================================================
# ECR Repository
# =============================================================================

resource "aws_ecr_repository" "app_repository" {
  name                 = var.project_name
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name        = "${var.project_name}-ecr"
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_ecr_lifecycle_policy" "app_repository_policy" {
  repository = aws_ecr_repository.app_repository.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep last 10 images"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 10
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}

# =============================================================================
# EC2 Instance
# =============================================================================

resource "aws_instance" "web_server" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.instance_type
  key_name              = var.key_name
  vpc_security_group_ids = [aws_security_group.web_server.id]
  subnet_id             = aws_subnet.public[0].id
  iam_instance_profile  = aws_iam_instance_profile.ec2_profile.name

  root_block_device {
    volume_type = "gp3"
    volume_size = 30
    throughput  = 125
    iops       = 3000
    encrypted  = true
  }

  user_data = base64encode(templatefile("${path.module}/user-data.sh", {
    project_name = var.project_name
    environment  = var.environment
  }))

  tags = {
    Name        = "${var.project_name}-web-server"
    Environment = var.environment
    Project     = var.project_name
    Role        = "web-server"
  }
}

# =============================================================================
# CodeDeploy Application
# =============================================================================

resource "aws_codedeploy_app" "app" {
  compute_platform = "Server"
  name            = var.project_name

  tags = {
    Name        = "${var.project_name}-codedeploy-app"
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_codedeploy_deployment_group" "app_deployment_group" {
  app_name              = aws_codedeploy_app.app.name
  deployment_group_name = "production"
  service_role_arn     = aws_iam_role.codedeploy_role.arn

  deployment_config_name = "CodeDeploy.ServerAllAtOne"

  ec2_tag_filter {
    key   = "Name"
    type  = "KEY_AND_VALUE"
    value = aws_instance.web_server.tags.Name
  }

  auto_rollback_configuration {
    enabled = true
    events  = ["DEPLOYMENT_FAILURE"]
  }

  blue_green_deployment_config {
    terminate_blue_instances_on_deployment_success {
      action                         = "TERMINATE"
      termination_wait_time_in_minutes = 1
    }

    deployment_ready_option {
      action_on_timeout = "CONTINUE_DEPLOYMENT"
    }

    green_fleet_provisioning_option {
      action = "COPY_AUTO_SCALING_GROUP"
    }
  }

  tags = {
    Name        = "${var.project_name}-deployment-group"
    Environment = var.environment
    Project     = var.project_name
  }
}

# =============================================================================
# Outputs
# =============================================================================

output "instance_public_ip" {
  description = "EC2 인스턴스의 공인 IP"
  value       = aws_instance.web_server.public_ip
}

output "instance_public_dns" {
  description = "EC2 인스턴스의 공인 DNS"
  value       = aws_instance.web_server.public_dns
}

output "ecr_repository_url" {
  description = "ECR 리포지토리 URL"
  value       = aws_ecr_repository.app_repository.repository_url
}

output "codedeploy_application_name" {
  description = "CodeDeploy 애플리케이션 이름"
  value       = aws_codedeploy_app.app.name
}

output "codedeploy_deployment_group_name" {
  description = "CodeDeploy 배포 그룹 이름"
  value       = aws_codedeploy_deployment_group.app_deployment_group.deployment_group_name
}

output "application_urls" {
  description = "애플리케이션 접근 URL들"
  value = {
    web_app     = "http://${aws_instance.web_server.public_ip}"
    health_check = "http://${aws_instance.web_server.public_ip}/api/system/health"
    swagger_ui   = "http://${aws_instance.web_server.public_ip}/api/swagger-ui.html"
  }
}