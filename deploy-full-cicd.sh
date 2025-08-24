#!/bin/bash

# =============================================================================
# Complete CI/CD Infrastructure Deployment Script
# =============================================================================
# 이 스크립트는 Ubuntu 24.04 기반 Spring Boot CI/CD 인프라를 완전 자동화 배포합니다
# - AWS 인프라 생성 (Terraform)
# - EC2 인스턴스 초기 설정
# - GitHub Actions Runner 설정
# - 첫 번째 배포 실행
# - 검증 및 테스트
# =============================================================================

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m'

# 로깅 함수
log() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_header() {
    echo
    echo -e "${PURPLE}===============================================================================${NC}"
    echo -e "${PURPLE} $1${NC}"
    echo -e "${PURPLE}===============================================================================${NC}"
    echo
}

# 설정 변수들
PROJECT_NAME="iroom-backend"
AWS_REGION="ap-northeast-2"
TERRAFORM_DIR="./terraform"
GITHUB_REPO_URL=""
AWS_KEY_PAIR=""
GITHUB_TOKEN=""

# 필수 도구 확인
check_prerequisites() {
    log_header "필수 도구 확인"
    
    local missing_tools=()
    
    if ! command -v aws &> /dev/null; then
        missing_tools+=("aws-cli")
    fi
    
    if ! command -v terraform &> /dev/null; then
        missing_tools+=("terraform")
    fi
    
    if ! command -v jq &> /dev/null; then
        missing_tools+=("jq")
    fi
    
    if ! command -v git &> /dev/null; then
        missing_tools+=("git")
    fi
    
    if [ ${#missing_tools[@]} -ne 0 ]; then
        log_error "다음 도구들이 설치되어 있지 않습니다:"
        for tool in "${missing_tools[@]}"; do
            echo "  • $tool"
        done
        echo
        echo "설치 방법:"
        echo "  • aws-cli: https://aws.amazon.com/cli/"
        echo "  • terraform: https://terraform.io/downloads"
        echo "  • jq: sudo apt install jq (Ubuntu) 또는 brew install jq (macOS)"
        exit 1
    fi
    
    log_success "모든 필수 도구가 설치되어 있습니다"
}

# 사용자 입력 받기
get_user_inputs() {
    log_header "배포 정보 입력"
    
    echo "다음 정보를 입력해주세요:"
    echo
    
    # GitHub 리포지토리 URL
    if [ -z "$GITHUB_REPO_URL" ]; then
        read -p "GitHub 리포지토리 URL (예: https://github.com/user/repo): " GITHUB_REPO_URL
        if [ -z "$GITHUB_REPO_URL" ]; then
            log_error "GitHub 리포지토리 URL은 필수입니다"
            exit 1
        fi
    fi
    
    # AWS Key Pair
    if [ -z "$AWS_KEY_PAIR" ]; then
        echo
        echo "사용 가능한 AWS Key Pair 목록:"
        aws ec2 describe-key-pairs --region $AWS_REGION --query 'KeyPairs[*].KeyName' --output table 2>/dev/null || {
            log_warning "Key Pair 목록을 가져올 수 없습니다. AWS 자격 증명을 확인해주세요."
        }
        echo
        read -p "사용할 AWS Key Pair 이름: " AWS_KEY_PAIR
        if [ -z "$AWS_KEY_PAIR" ]; then
            log_error "AWS Key Pair 이름은 필수입니다"
            exit 1
        fi
    fi
    
    # GitHub Token
    if [ -z "$GITHUB_TOKEN" ]; then
        echo
        read -s -p "GitHub Personal Access Token (Self-hosted Runner 등록용): " GITHUB_TOKEN
        echo
        if [ -z "$GITHUB_TOKEN" ]; then
            log_error "GitHub Token은 필수입니다"
            exit 1
        fi
    fi
    
    echo
    log "입력된 정보:"
    log "  • GitHub 리포지토리: $GITHUB_REPO_URL"
    log "  • AWS Key Pair: $AWS_KEY_PAIR"
    log "  • AWS 리전: $AWS_REGION"
    echo
}

# AWS 자격 증명 확인
verify_aws_credentials() {
    log_header "AWS 자격 증명 확인"
    
    if ! aws sts get-caller-identity &> /dev/null; then
        log_error "AWS 자격 증명이 설정되지 않았습니다"
        echo "다음 명령어를 실행하여 AWS 자격 증명을 설정하세요:"
        echo "  aws configure"
        exit 1
    fi
    
    local aws_account=$(aws sts get-caller-identity --query Account --output text)
    local aws_user=$(aws sts get-caller-identity --query Arn --output text)
    
    log_success "AWS 자격 증명 확인 완료"
    log "  • 계정 ID: $aws_account"
    log "  • 사용자: $aws_user"
    echo
    
    # AWS Account ID를 환경 변수로 설정
    export AWS_ACCOUNT_ID=$aws_account
}

# Terraform 인프라 배포
deploy_infrastructure() {
    log_header "AWS 인프라 배포 (Terraform)"
    
    cd $TERRAFORM_DIR
    
    # Terraform 초기화
    log "Terraform 초기화 중..."
    terraform init
    
    # Terraform 계획 생성
    log "Terraform 계획 생성 중..."
    terraform plan \
        -var="key_name=$AWS_KEY_PAIR" \
        -var="github_repo_url=$GITHUB_REPO_URL" \
        -var="project_name=$PROJECT_NAME" \
        -out=tfplan
    
    # 사용자 확인
    echo
    read -p "위의 Terraform 계획을 실행하시겠습니까? (y/N): " confirm
    if [[ $confirm != [yY] ]]; then
        log_warning "배포가 취소되었습니다"
        exit 0
    fi
    
    # Terraform 적용
    log "Terraform 적용 중..."
    terraform apply tfplan
    
    # 출력값 저장
    terraform output -json > ../terraform-outputs.json
    
    cd ..
    
    log_success "AWS 인프라 배포 완료"
    
    # 인프라 정보 출력
    local instance_ip=$(jq -r '.instance_public_ip.value' terraform-outputs.json)
    local ecr_url=$(jq -r '.ecr_repository_url.value' terraform-outputs.json)
    
    log "배포된 인프라 정보:"
    log "  • EC2 Public IP: $instance_ip"
    log "  • ECR Repository: $ecr_url"
    
    export EC2_PUBLIC_IP=$instance_ip
    export ECR_REPOSITORY_URL=$ecr_url
    echo
}

# EC2 인스턴스 준비 대기
wait_for_instance() {
    log_header "EC2 인스턴스 초기화 대기"
    
    log "EC2 인스턴스가 완전히 초기화될 때까지 대기 중..."
    log "예상 소요 시간: 5-10분"
    
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        log "대기 중... ($attempt/$max_attempts) - $(date '+%H:%M:%S')"
        
        # SSH 연결 확인
        if ssh -i ~/.ssh/${AWS_KEY_PAIR}.pem -o StrictHostKeyChecking=no -o ConnectTimeout=10 ubuntu@$EC2_PUBLIC_IP "test -f /var/log/user-data-complete" &>/dev/null; then
            log_success "EC2 인스턴스 초기화 완료"
            return 0
        fi
        
        sleep 20
        ((attempt++))
    done
    
    log_error "EC2 인스턴스 초기화 시간 초과"
    log "수동으로 확인해주세요: ssh -i ~/.ssh/${AWS_KEY_PAIR}.pem ubuntu@$EC2_PUBLIC_IP"
    exit 1
}

# GitHub Actions Runner 설정
setup_github_runner() {
    log_header "GitHub Actions Self-hosted Runner 설정"
    
    # Runner 토큰 생성
    local repo_owner=$(echo $GITHUB_REPO_URL | cut -d'/' -f4)
    local repo_name=$(echo $GITHUB_REPO_URL | cut -d'/' -f5)
    
    log "GitHub Runner 토큰 생성 중..."
    local runner_token=$(curl -s -X POST \
        -H "Authorization: token $GITHUB_TOKEN" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/repos/$repo_owner/$repo_name/actions/runners/registration-token" | \
        jq -r .token)
    
    if [ "$runner_token" = "null" ] || [ -z "$runner_token" ]; then
        log_error "GitHub Runner 토큰 생성 실패"
        log "GitHub Token 권한을 확인해주세요 (repo, admin:repo_hook 권한 필요)"
        exit 1
    fi
    
    # EC2에서 Runner 설치 실행
    log "EC2에서 GitHub Runner 설치 중..."
    ssh -i ~/.ssh/${AWS_KEY_PAIR}.pem -o StrictHostKeyChecking=no ubuntu@$EC2_PUBLIC_IP << EOF
        cd /home/ubuntu
        ./install-github-runner.sh $GITHUB_REPO_URL $runner_token
EOF
    
    log_success "GitHub Actions Runner 설정 완료"
    echo
}

# GitHub Secrets 설정 안내
setup_github_secrets() {
    log_header "GitHub Repository Secrets 설정 안내"
    
    echo "GitHub 리포지토리에 다음 Secrets을 설정해주세요:"
    echo
    echo "🔐 필수 Secrets:"
    echo "  • AWS_ACCESS_KEY_ID: $(aws configure get aws_access_key_id)"
    echo "  • AWS_SECRET_ACCESS_KEY: [현재 설정된 Secret Key]"
    echo "  • AWS_ACCOUNT_ID: $AWS_ACCOUNT_ID"
    echo
    echo "📝 설정 방법:"
    echo "  1. GitHub 리포지토리 페이지로 이동"
    echo "  2. Settings > Secrets and variables > Actions"
    echo "  3. 'New repository secret' 클릭하여 위 Secrets 추가"
    echo
    
    read -p "GitHub Secrets 설정을 완료했습니까? (y/N): " confirm
    if [[ $confirm != [yY] ]]; then
        log_warning "GitHub Secrets을 설정한 후 다시 실행하세요"
        exit 0
    fi
    
    log_success "GitHub Secrets 설정 확인됨"
    echo
}

# 첫 번째 배포 실행
trigger_first_deployment() {
    log_header "첫 번째 배포 실행"
    
    log "GitHub에 빈 커밋을 푸시하여 CI/CD 파이프라인을 트리거합니다..."
    
    # 현재 브랜치 확인
    local current_branch=$(git rev-parse --abbrev-ref HEAD)
    
    # 빈 커밋 생성 및 푸시
    git commit --allow-empty -m "🚀 Initial CI/CD deployment trigger"
    git push origin $current_branch
    
    log_success "배포 파이프라인이 트리거되었습니다"
    log "GitHub Actions 페이지에서 진행 상황을 확인하세요: $GITHUB_REPO_URL/actions"
    echo
}

# 배포 완료 검증
verify_deployment() {
    log_header "배포 완료 검증"
    
    log "배포 완료를 기다리는 중... (최대 15분)"
    
    local max_attempts=45
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        log "검증 시도 $attempt/$max_attempts"
        
        # 헬스체크 엔드포인트 확인
        if curl -f -s "http://$EC2_PUBLIC_IP/api/system/health" > /dev/null; then
            log_success "🎉 배포 완료! 서비스가 정상적으로 실행 중입니다"
            
            echo
            log "🌐 서비스 접근 URL:"
            log "  • 메인 페이지: http://$EC2_PUBLIC_IP"
            log "  • Health Check: http://$EC2_PUBLIC_IP/api/system/health"
            log "  • API 문서: http://$EC2_PUBLIC_IP/api/swagger-ui.html"
            
            return 0
        fi
        
        sleep 20
        ((attempt++))
    done
    
    log_warning "자동 검증 시간이 초과되었습니다"
    log "수동으로 확인해주세요:"
    log "  • GitHub Actions: $GITHUB_REPO_URL/actions"
    log "  • EC2 로그: ssh -i ~/.ssh/${AWS_KEY_PAIR}.pem ubuntu@$EC2_PUBLIC_IP 'tail -f /opt/logs/deployment.log'"
    
    return 1
}

# 배포 정보 출력
print_deployment_summary() {
    log_header "배포 완료 요약"
    
    echo "🎉 Ubuntu 24.04 기반 Spring Boot CI/CD 인프라가 성공적으로 구축되었습니다!"
    echo
    
    echo "📋 배포된 구성 요소:"
    echo "  ✅ EC2 인스턴스 (Ubuntu 24.04)"
    echo "  ✅ Docker & Docker Compose"
    echo "  ✅ Java 21 (Bellsoft Liberica)"
    echo "  ✅ Nginx (Blue-Green 배포 지원)"
    echo "  ✅ GitHub Actions Self-hosted Runner"
    echo "  ✅ AWS CodeDeploy Agent"
    echo "  ✅ ECR Repository"
    echo "  ✅ 모니터링 도구 (Node Exporter)"
    echo
    
    echo "🌐 서비스 URL:"
    echo "  • 메인 서비스: http://$EC2_PUBLIC_IP"
    echo "  • Health Check: http://$EC2_PUBLIC_IP/api/system/health"
    echo "  • API 문서: http://$EC2_PUBLIC_IP/api/swagger-ui.html"
    echo "  • Blue 환경: http://$EC2_PUBLIC_IP/health/blue"
    echo "  • Green 환경: http://$EC2_PUBLIC_IP/health/green"
    echo
    
    echo "🔧 관리 명령어:"
    echo "  • SSH 접속: ssh -i ~/.ssh/${AWS_KEY_PAIR}.pem ubuntu@$EC2_PUBLIC_IP"
    echo "  • 배포 상태: /opt/scripts/blue-green-deploy.sh status"
    echo "  • 헬스체크: /opt/scripts/health-check.sh"
    echo "  • 롤백: /opt/scripts/blue-green-deploy.sh rollback"
    echo
    
    echo "📊 모니터링:"
    echo "  • GitHub Actions: $GITHUB_REPO_URL/actions"
    echo "  • 배포 로그: /opt/logs/deployment.log"
    echo "  • 시스템 로그: /opt/logs/health-check.log"
    echo
    
    echo "🚀 다음 단계:"
    echo "  1. 코드를 main 브랜치에 푸시하면 자동 배포됩니다"
    echo "  2. GitHub Actions에서 배포 진행 상황을 모니터링하세요"
    echo "  3. 문제 발생 시 'Manual Rollback' 워크플로우를 사용하세요"
    echo
    
    log_success "🎊 모든 설정이 완료되었습니다! 이제 git push만으로 자동 배포가 가능합니다."
}

# 메인 실행 함수
main() {
    log_header "Ubuntu 24.04 기반 Spring Boot CI/CD 인프라 자동 구축"
    
    echo "이 스크립트는 다음 작업을 수행합니다:"
    echo "  1. ✅ 필수 도구 확인"
    echo "  2. 🔐 AWS 자격 증명 확인"
    echo "  3. ☁️  AWS 인프라 배포 (EC2, ECR, CodeDeploy)"
    echo "  4. ⚙️  EC2 인스턴스 초기화 대기"
    echo "  5. 🔄 GitHub Actions Runner 설정"
    echo "  6. 🔑 GitHub Secrets 설정 안내"
    echo "  7. 🚀 첫 번째 배포 실행"
    echo "  8. ✅ 배포 완료 검증"
    echo
    
    read -p "계속 진행하시겠습니까? (y/N): " confirm
    if [[ $confirm != [yY] ]]; then
        log "배포가 취소되었습니다"
        exit 0
    fi
    
    check_prerequisites
    get_user_inputs
    verify_aws_credentials
    deploy_infrastructure
    wait_for_instance
    setup_github_runner
    setup_github_secrets
    trigger_first_deployment
    
    if verify_deployment; then
        print_deployment_summary
    else
        log_warning "자동 검증에 실패했지만 인프라는 구축되었습니다"
        log "수동으로 상태를 확인해보세요"
    fi
}

# 오류 처리
trap 'log_error "스크립트 실행 중 오류가 발생했습니다. 라인 $LINENO에서 종료됨"' ERR

# 스크립트 실행
main "$@"