#!/bin/bash

# =============================================================================
# CodeDeploy Hook: BeforeInstall
# =============================================================================
# 배포 전 실행되는 스크립트
# - 시스템 상태 확인
# - 필요한 서비스 상태 검증
# - 배포 준비 작업
# =============================================================================

set -e

LOG_FILE="/opt/logs/codedeploy-before-install.log"
mkdir -p "$(dirname "$LOG_FILE")"

# 로깅 함수
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

log "🚀 CodeDeploy BeforeInstall 단계 시작"

# Docker 서비스 확인
log "Docker 서비스 상태 확인..."
if ! systemctl is-active --quiet docker; then
    log "Docker 서비스 시작 중..."
    systemctl start docker
    sleep 5
fi

# 필수 디렉토리 생성
log "필수 디렉토리 생성..."
mkdir -p /opt/applications/iroom-backend
mkdir -p /opt/logs
mkdir -p /opt/backups
chown -R ubuntu:ubuntu /opt/applications/iroom-backend
chown -R ubuntu:ubuntu /opt/logs

# Nginx 서비스 확인
log "Nginx 서비스 상태 확인..."
if ! systemctl is-active --quiet nginx; then
    log "Nginx 서비스 시작 중..."
    systemctl start nginx
fi

# ECR 로그인 (Docker가 이미지를 풀링할 수 있도록)
log "ECR 로그인 실행..."
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com || {
    log "❌ ECR 로그인 실패"
    exit 1
}

# 기존 애플리케이션 백업 (필요시)
if [ -d "/opt/applications/iroom-backend/current" ]; then
    log "기존 애플리케이션 백업 중..."
    backup_dir="/opt/backups/backup-$(date +%Y%m%d-%H%M%S)"
    mkdir -p "$backup_dir"
    cp -r /opt/applications/iroom-backend/current/* "$backup_dir/" || true
fi

# 시스템 리소스 확인
log "시스템 리소스 확인..."
df -h /opt
free -h

log "✅ BeforeInstall 단계 완료"