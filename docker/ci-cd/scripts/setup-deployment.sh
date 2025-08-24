#!/bin/bash

# =============================================================================
# EC2 Initial Setup Script for CI/CD Environment
# =============================================================================
# 이 스크립트는 EC2 인스턴스에서 실행되어 완전한 CI/CD 환경을 구축합니다
# - Docker 및 필수 도구 설치
# - GitHub Actions Self-hosted Runner 설정
# - AWS CLI 및 CodeDeploy Agent 구성
# - Nginx 및 모니터링 도구 설정
# - 보안 설정 및 방화벽 구성
# =============================================================================

set -e

# 환경 변수 설정
export DEBIAN_FRONTEND=noninteractive
export TZ=Asia/Seoul

# 컬러 출력
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

# 루트 권한 확인
check_root() {
    if [[ $EUID -ne 0 ]]; then
        log_error "이 스크립트는 root 권한으로 실행되어야 합니다"
        exit 1
    fi
}

# 시스템 업데이트
update_system() {
    log "시스템 패키지 업데이트 시작..."
    
    apt-get update -y
    apt-get upgrade -y
    apt-get install -y \
        apt-transport-https \
        ca-certificates \
        curl \
        gnupg \
        lsb-release \
        software-properties-common \
        wget \
        unzip \
        git \
        jq \
        vim \
        htop \
        net-tools \
        telnet \
        build-essential \
        python3 \
        python3-pip \
        ruby-full \
        nodejs \
        npm \
        supervisor \
        ufw \
        fail2ban \
        logrotate
    
    # 시간대 설정
    timedatectl set-timezone $TZ
    
    log_success "시스템 업데이트 완료"
}

# Docker 설치
install_docker() {
    log "Docker 설치 시작..."
    
    # Docker 공식 GPG 키 추가
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
    
    # Docker 저장소 추가
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
    
    # Docker 설치
    apt-get update -y
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    
    # Docker 서비스 시작 및 활성화
    systemctl start docker
    systemctl enable docker
    
    # ubuntu 사용자를 docker 그룹에 추가
    usermod -aG docker ubuntu || true
    
    # Docker 설치 확인
    docker --version
    docker compose version
    
    log_success "Docker 설치 완료"
}

# Java 21 설치
install_java() {
    log "Java 21 설치 시작..."
    
    # Bellsoft Liberica JDK 저장소 추가
    wget -q -O - https://download.bell-sw.com/pki/GPG-KEY-bellsoft | apt-key add -
    echo "deb [arch=amd64] https://apt.bell-sw.com/ stable main" | tee /etc/apt/sources.list.d/bellsoft.list
    
    # Java 설치
    apt-get update -y
    apt-get install -y bellsoft-java21-full
    
    # JAVA_HOME 설정
    echo 'export JAVA_HOME=/usr/lib/jvm/bellsoft-java21-full-amd64' >> /etc/environment
    echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /etc/environment
    
    # Java 설치 확인
    java -version
    
    log_success "Java 21 설치 완료"
}

# AWS CLI 설치
install_aws_cli() {
    log "AWS CLI 설치 시작..."
    
    # AWS CLI v2 다운로드 및 설치
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
    unzip awscliv2.zip
    ./aws/install
    rm -rf aws awscliv2.zip
    
    # AWS CLI 설치 확인
    aws --version
    
    log_success "AWS CLI 설치 완료"
}

# CodeDeploy Agent 설치
install_codedeploy_agent() {
    log "CodeDeploy Agent 설치 시작..."
    
    # CodeDeploy Agent 다운로드 및 설치
    cd /home/ubuntu
    wget https://aws-codedeploy-ap-northeast-2.s3.ap-northeast-2.amazonaws.com/latest/install
    chmod +x ./install
    ./install auto
    
    # 서비스 시작 및 활성화
    systemctl start codedeploy-agent
    systemctl enable codedeploy-agent
    
    # 상태 확인
    systemctl status codedeploy-agent --no-pager
    
    log_success "CodeDeploy Agent 설치 완료"
}

# Nginx 설치 및 설정
install_nginx() {
    log "Nginx 설치 및 설정 시작..."
    
    # Nginx 설치
    apt-get install -y nginx
    
    # 기본 설정 백업
    cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.backup
    
    # 설정 디렉토리 생성
    mkdir -p /etc/nginx/conf.d
    
    # 기본 업스트림 설정 생성
    cat > /etc/nginx/conf.d/upstream.conf << 'EOF'
# Default upstream configuration
upstream backend {
    server localhost:8080 max_fails=3 fail_timeout=30s;
    keepalive 32;
}
EOF
    
    # 기본 HTML 페이지 생성
    cat > /var/www/html/index.html << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>iRoom Backend Server</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 50px; }
        .status { padding: 20px; background: #f0f0f0; border-radius: 5px; }
        .healthy { background: #d4edda; color: #155724; }
        .unhealthy { background: #f8d7da; color: #721c24; }
    </style>
</head>
<body>
    <h1>iRoom Backend Server</h1>
    <div class="status" id="status">
        <p>서버 상태를 확인하는 중...</p>
    </div>
    
    <script>
        function checkHealth() {
            fetch('/api/system/health')
                .then(response => response.json())
                .then(data => {
                    const statusDiv = document.getElementById('status');
                    statusDiv.className = 'status healthy';
                    statusDiv.innerHTML = '<h3>✅ 서비스 정상 동작 중</h3><p>Status: ' + data.status + '</p>';
                })
                .catch(error => {
                    const statusDiv = document.getElementById('status');
                    statusDiv.className = 'status unhealthy';
                    statusDiv.innerHTML = '<h3>❌ 서비스 연결 실패</h3><p>백엔드 서비스가 시작되지 않았습니다.</p>';
                });
        }
        
        checkHealth();
        setInterval(checkHealth, 30000);
    </script>
</body>
</html>
EOF
    
    # Nginx 설정 테스트
    nginx -t
    
    # 서비스 시작 및 활성화
    systemctl start nginx
    systemctl enable nginx
    
    log_success "Nginx 설치 및 설정 완료"
}

# 모니터링 도구 설치
install_monitoring() {
    log "모니터링 도구 설치 시작..."
    
    # Node Exporter 설치
    wget https://github.com/prometheus/node_exporter/releases/download/v1.8.2/node_exporter-1.8.2.linux-amd64.tar.gz
    tar xvfz node_exporter-1.8.2.linux-amd64.tar.gz
    mv node_exporter-1.8.2.linux-amd64/node_exporter /usr/local/bin/
    rm -rf node_exporter-1.8.2.linux-amd64*
    
    # Node Exporter 서비스 생성
    cat > /etc/systemd/system/node-exporter.service << 'EOF'
[Unit]
Description=Node Exporter
After=network.target

[Service]
Type=simple
ExecStart=/usr/local/bin/node_exporter
User=nobody
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF
    
    # 서비스 시작 및 활성화
    systemctl daemon-reload
    systemctl start node-exporter
    systemctl enable node-exporter
    
    log_success "모니터링 도구 설치 완료"
}

# 보안 설정
configure_security() {
    log "보안 설정 시작..."
    
    # UFW 방화벽 설정
    ufw --force reset
    ufw default deny incoming
    ufw default allow outgoing
    
    # 필요한 포트 허용
    ufw allow ssh
    ufw allow 80/tcp    # HTTP
    ufw allow 443/tcp   # HTTPS
    ufw allow 3055/tcp  # Spring Boot (임시)
    ufw allow 8080/tcp  # Blue port (임시)
    ufw allow 8081/tcp  # Green port (임시)
    ufw allow 9100/tcp  # Node Exporter
    
    # 방화벽 활성화
    ufw --force enable
    
    # Fail2Ban 설정
    cat > /etc/fail2ban/jail.local << 'EOF'
[DEFAULT]
bantime = 1800
findtime = 600
maxretry = 5

[sshd]
enabled = true
port = ssh
filter = sshd
logpath = /var/log/auth.log
maxretry = 3

[nginx-http-auth]
enabled = true
filter = nginx-http-auth
logpath = /var/log/nginx/error.log
maxretry = 6

[nginx-noscript]
enabled = true
port = http,https
filter = nginx-noscript
logpath = /var/log/nginx/access.log
maxretry = 6
EOF
    
    # Fail2Ban 재시작
    systemctl restart fail2ban
    
    # SSH 보안 강화
    sed -i 's/#PasswordAuthentication yes/PasswordAuthentication no/' /etc/ssh/sshd_config
    sed -i 's/#PubkeyAuthentication yes/PubkeyAuthentication yes/' /etc/ssh/sshd_config
    systemctl restart ssh
    
    log_success "보안 설정 완료"
}

# 로그 로테이션 설정
configure_logging() {
    log "로그 설정 시작..."
    
    # 애플리케이션 로그 디렉토리 생성
    mkdir -p /opt/logs /opt/applications /opt/backups /opt/monitoring
    chown -R ubuntu:ubuntu /opt/
    
    # 로그 로테이션 설정
    cat > /etc/logrotate.d/iroom-backend << 'EOF'
/opt/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 644 ubuntu ubuntu
    postrotate
        systemctl reload nginx || true
    endscript
}

/var/log/nginx/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 644 www-data adm
    postrotate
        systemctl reload nginx || true
    endscript
}
EOF
    
    log_success "로그 설정 완료"
}

# GitHub Actions Runner 설정 스크립트 생성
create_github_runner_script() {
    log "GitHub Actions Runner 설정 스크립트 생성..."
    
    cat > /home/ubuntu/install-github-runner.sh << 'EOF'
#!/bin/bash
# GitHub Actions Self-hosted Runner 설치 스크립트
# 사용법: ./install-github-runner.sh <REPO_URL> <RUNNER_TOKEN>

set -e

if [ $# -ne 2 ]; then
    echo "사용법: $0 <REPO_URL> <RUNNER_TOKEN>"
    echo "예시: $0 https://github.com/user/repo ABCDEF123456789"
    exit 1
fi

REPO_URL=$1
RUNNER_TOKEN=$2

# Runner 다운로드 및 설정
cd /home/ubuntu
mkdir -p actions-runner && cd actions-runner

# 최신 runner 다운로드
curl -o actions-runner-linux-x64-2.317.0.tar.gz -L https://github.com/actions/runner/releases/download/v2.317.0/actions-runner-linux-x64-2.317.0.tar.gz
tar xzf ./actions-runner-linux-x64-2.317.0.tar.gz

# 의존성 설치
sudo ./bin/installdependencies.sh

# Runner 설정
./config.sh --url $REPO_URL --token $RUNNER_TOKEN --name "ec2-runner" --work _work --unattended

# 서비스로 설치
sudo ./svc.sh install ubuntu
sudo ./svc.sh start

echo "GitHub Actions Runner 설치 완료"
EOF
    
    chmod +x /home/ubuntu/install-github-runner.sh
    chown ubuntu:ubuntu /home/ubuntu/install-github-runner.sh
    
    log_success "GitHub Actions Runner 설정 스크립트 생성 완료"
}

# 헬스체크 스크립트 생성
create_health_check_script() {
    log "헬스체크 스크립트 생성..."
    
    cat > /opt/scripts/health-check.sh << 'EOF'
#!/bin/bash
# 시스템 헬스체크 스크립트

check_service() {
    local service=$1
    if systemctl is-active --quiet $service; then
        echo "✅ $service: RUNNING"
        return 0
    else
        echo "❌ $service: NOT RUNNING"
        return 1
    fi
}

check_port() {
    local port=$1
    local name=$2
    if nc -z localhost $port 2>/dev/null; then
        echo "✅ $name (포트 $port): OPEN"
        return 0
    else
        echo "❌ $name (포트 $port): CLOSED"
        return 1
    fi
}

echo "=== 시스템 헬스체크 ==="
date

echo
echo "=== 서비스 상태 ==="
check_service docker
check_service nginx
check_service codedeploy-agent
check_service node-exporter

echo
echo "=== 포트 상태 ==="
check_port 80 "HTTP"
check_port 8080 "Spring Boot Blue"
check_port 8081 "Spring Boot Green"
check_port 9100 "Node Exporter"

echo
echo "=== 디스크 사용량 ==="
df -h /

echo
echo "=== 메모리 사용량 ==="
free -h

echo
echo "=== 시스템 로드 ==="
uptime
EOF
    
    chmod +x /opt/scripts/health-check.sh
    
    # 크론 작업 추가 (매 5분마다 헬스체크)
    echo "*/5 * * * * /opt/scripts/health-check.sh >> /opt/logs/health-check.log 2>&1" | crontab -u ubuntu -
    
    log_success "헬스체크 스크립트 생성 완료"
}

# 디렉토리 구조 생성
create_directory_structure() {
    log "디렉토리 구조 생성..."
    
    # 필요한 디렉토리들 생성
    mkdir -p /opt/{applications,logs,backups,monitoring,scripts}
    mkdir -p /opt/applications/iroom-backend
    
    # 권한 설정
    chown -R ubuntu:ubuntu /opt/
    chmod -R 755 /opt/scripts/
    
    log_success "디렉토리 구조 생성 완료"
}

# 환경 정보 출력
print_environment_info() {
    log_success "=== EC2 CI/CD 환경 구축 완료 ==="
    
    echo
    echo "📋 설치된 구성 요소:"
    echo "  • Docker & Docker Compose"
    echo "  • Java 21 (Bellsoft Liberica)"
    echo "  • AWS CLI v2"
    echo "  • CodeDeploy Agent"
    echo "  • Nginx (포트 80)"
    echo "  • Node Exporter (포트 9100)"
    echo "  • UFW 방화벽"
    echo "  • Fail2Ban"
    echo
    echo "🔧 중요한 경로:"
    echo "  • 애플리케이션: /opt/applications/iroom-backend"
    echo "  • 로그: /opt/logs"
    echo "  • 백업: /opt/backups"
    echo "  • 스크립트: /opt/scripts"
    echo "  • Nginx 설정: /etc/nginx/conf.d"
    echo
    echo "📝 다음 단계:"
    echo "  1. GitHub Actions Runner 설정:"
    echo "     sudo su - ubuntu"
    echo "     ./install-github-runner.sh <REPO_URL> <RUNNER_TOKEN>"
    echo
    echo "  2. AWS 자격 증명 설정:"
    echo "     aws configure"
    echo
    echo "  3. 배포 테스트:"
    echo "     /opt/scripts/blue-green-deploy.sh status"
    echo
    echo "  4. 헬스체크 확인:"
    echo "     /opt/scripts/health-check.sh"
    echo
    
    log_success "설정이 완료되었습니다. 서버를 재부팅하는 것을 권장합니다."
}

# 메인 실행 함수
main() {
    log "EC2 CI/CD 환경 구축 시작..."
    
    check_root
    update_system
    install_docker
    install_java
    install_aws_cli
    install_codedeploy_agent
    install_nginx
    install_monitoring
    configure_security
    configure_logging
    create_directory_structure
    create_github_runner_script
    create_health_check_script
    
    print_environment_info
}

# 스크립트 실행
main "$@"