#!/bin/bash

# =============================================================================
# EC2 User Data Script - 인스턴스 초기 설정
# =============================================================================
# 이 스크립트는 EC2 인스턴스가 시작될 때 자동으로 실행되어
# CI/CD 환경을 구축합니다.
# =============================================================================

set -e

# 환경 변수 (Terraform에서 전달)
PROJECT_NAME="${project_name}"
ENVIRONMENT="${environment}"

# 로깅 설정
LOG_FILE="/var/log/user-data.log"
exec > >(tee -a $LOG_FILE)
exec 2>&1

echo "=================================================================="
echo "EC2 User Data Script 시작: $(date)"
echo "Project: $PROJECT_NAME"
echo "Environment: $ENVIRONMENT"
echo "=================================================================="

# 시스템 업데이트
echo "시스템 패키지 업데이트..."
export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get upgrade -y

# 필수 패키지 설치
echo "필수 패키지 설치..."
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
    supervisor \
    ufw \
    fail2ban \
    awscli \
    ruby-full

# 시간대 설정
timedatectl set-timezone Asia/Seoul

# Docker 설치
echo "Docker 설치 중..."
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

systemctl start docker
systemctl enable docker
usermod -aG docker ubuntu

# Java 21 설치 (Bellsoft Liberica)
echo "Java 21 설치 중..."
wget -q -O - https://download.bell-sw.com/pki/GPG-KEY-bellsoft | apt-key add -
echo "deb [arch=amd64] https://apt.bell-sw.com/ stable main" | tee /etc/apt/sources.list.d/bellsoft.list
apt-get update -y
apt-get install -y bellsoft-java21-full

# JAVA_HOME 설정
echo 'export JAVA_HOME=/usr/lib/jvm/bellsoft-java21-full-amd64' >> /etc/environment
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /etc/environment

# CodeDeploy Agent 설치
echo "CodeDeploy Agent 설치 중..."
cd /home/ubuntu
wget https://aws-codedeploy-ap-northeast-2.s3.ap-northeast-2.amazonaws.com/latest/install
chmod +x ./install
./install auto

systemctl start codedeploy-agent
systemctl enable codedeploy-agent

# Nginx 설치
echo "Nginx 설치 중..."
apt-get install -y nginx
systemctl start nginx
systemctl enable nginx

# 디렉토리 구조 생성
echo "디렉토리 구조 생성 중..."
mkdir -p /opt/applications /opt/logs /opt/backups /opt/monitoring /opt/scripts
chown -R ubuntu:ubuntu /opt/

# 기본 설정 파일 다운로드
echo "설정 파일 다운로드 중..."
cd /tmp

# GitHub에서 설정 파일들 다운로드 (실제로는 S3 버킷 사용 권장)
# 여기서는 예시로 curl 명령어로 raw 파일 다운로드

# Blue-Green 배포 스크립트
wget -O /opt/scripts/blue-green-deploy.sh https://raw.githubusercontent.com/your-repo/iroom-backend/main/docker/ci-cd/scripts/blue-green-deploy.sh
chmod +x /opt/scripts/blue-green-deploy.sh

# Nginx 설정
mkdir -p /etc/nginx/conf.d
cat > /etc/nginx/conf.d/upstream.conf << 'EOF'
upstream backend {
    server localhost:8080 max_fails=3 fail_timeout=30s;
    keepalive 32;
}
EOF

# 기본 Nginx 사이트 설정
cat > /etc/nginx/sites-available/default << 'EOF'
server {
    listen 80 default_server;
    listen [::]:80 default_server;
    server_name _;

    location /api/ {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
        
        proxy_http_version 1.1;
        proxy_set_header Connection "";
    }

    location /health/blue {
        proxy_pass http://localhost:8080/api/system/health;
        proxy_connect_timeout 5s;
        proxy_send_timeout 5s;
        proxy_read_timeout 5s;
    }

    location /health/green {
        proxy_pass http://localhost:8081/api/system/health;
        proxy_connect_timeout 5s;
        proxy_send_timeout 5s;
        proxy_read_timeout 5s;
    }

    location / {
        root /var/www/html;
        index index.html index.htm;
        try_files $uri $uri/ @fallback;
    }

    location @fallback {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

nginx -t && systemctl reload nginx

# 방화벽 설정
echo "방화벽 설정 중..."
ufw --force reset
ufw default deny incoming
ufw default allow outgoing
ufw allow ssh
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

# Node Exporter 설치 (모니터링)
echo "Node Exporter 설치 중..."
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

systemctl daemon-reload
systemctl start node-exporter
systemctl enable node-exporter

# 헬스체크 스크립트 생성
cat > /opt/scripts/health-check.sh << 'EOF'
#!/bin/bash
echo "=== System Health Check ==="
date
echo
echo "Services:"
systemctl is-active --quiet docker && echo "✅ Docker: RUNNING" || echo "❌ Docker: NOT RUNNING"
systemctl is-active --quiet nginx && echo "✅ Nginx: RUNNING" || echo "❌ Nginx: NOT RUNNING"
systemctl is-active --quiet codedeploy-agent && echo "✅ CodeDeploy Agent: RUNNING" || echo "❌ CodeDeploy Agent: NOT RUNNING"
echo
echo "Disk Usage:"
df -h /opt
echo
echo "Memory Usage:"
free -h
EOF

chmod +x /opt/scripts/health-check.sh

# 크론 작업 설정 (헬스체크)
echo "*/5 * * * * /opt/scripts/health-check.sh >> /opt/logs/health-check.log 2>&1" | crontab -u ubuntu -

# 기본 웹페이지 생성
cat > /var/www/html/index.html << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>iRoom Backend Server</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 50px; background: #f5f5f5; }
        .container { background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .status { padding: 20px; margin: 20px 0; border-radius: 5px; }
        .healthy { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .unhealthy { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .loading { background: #fff3cd; color: #856404; border: 1px solid #ffeaa7; }
        h1 { color: #333; }
        .info { background: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🏠 iRoom Backend Server</h1>
        <div class="info">
            <h3>📋 Server Information</h3>
            <p><strong>Environment:</strong> Production</p>
            <p><strong>Instance:</strong> EC2 Ubuntu 24.04</p>
            <p><strong>Deployment:</strong> Blue-Green Strategy</p>
        </div>
        
        <div class="status loading" id="status">
            <h3>⏳ Checking server status...</h3>
            <p>Please wait while we verify the backend service...</p>
        </div>

        <div class="info">
            <h3>🔗 Quick Links</h3>
            <ul>
                <li><a href="/api/system/health" target="_blank">Health Check API</a></li>
                <li><a href="/api/swagger-ui.html" target="_blank">API Documentation (Swagger UI)</a></li>
                <li><a href="/health/blue" target="_blank">Blue Environment Health</a></li>
                <li><a href="/health/green" target="_blank">Green Environment Health</a></li>
            </ul>
        </div>
    </div>
    
    <script>
        async function checkHealth() {
            try {
                const response = await fetch('/api/system/health');
                const data = await response.json();
                const statusDiv = document.getElementById('status');
                
                if (data.status === 'UP') {
                    statusDiv.className = 'status healthy';
                    statusDiv.innerHTML = `
                        <h3>✅ Backend Service is Healthy</h3>
                        <p><strong>Status:</strong> ${data.status}</p>
                        <p><strong>Timestamp:</strong> ${new Date().toLocaleString()}</p>
                    `;
                } else {
                    throw new Error('Service is not healthy');
                }
            } catch (error) {
                const statusDiv = document.getElementById('status');
                statusDiv.className = 'status unhealthy';
                statusDiv.innerHTML = `
                    <h3>❌ Backend Service Connection Failed</h3>
                    <p>The Spring Boot application may not be running yet.</p>
                    <p><strong>Note:</strong> If this is a new deployment, please wait a few minutes for the service to start.</p>
                `;
            }
        }
        
        // Initial check
        checkHealth();
        
        // Check every 30 seconds
        setInterval(checkHealth, 30000);
    </script>
</body>
</html>
EOF

# 소유권 설정
chown -R ubuntu:ubuntu /opt/
chown -R www-data:www-data /var/www/html/

# 서비스 재시작
systemctl restart nginx
systemctl restart docker

echo "=================================================================="
echo "EC2 User Data Script 완료: $(date)"
echo "=================================================================="

# 완료 신호
touch /var/log/user-data-complete